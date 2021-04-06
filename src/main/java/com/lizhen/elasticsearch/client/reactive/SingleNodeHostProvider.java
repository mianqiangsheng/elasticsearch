/*
 * Copyright 2018-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lizhen.elasticsearch.client.reactive;

import com.lizhen.elasticsearch.client.reactive.HostProvider;
import com.lizhen.elasticsearch.client.reactive.WebClientProvider;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.function.Supplier;

import com.lizhen.elasticsearch.client.ElasticsearchHost;
import com.lizhen.elasticsearch.client.ElasticsearchHost.State;
import com.lizhen.elasticsearch.client.NoReachableHostException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link HostProvider} for a single host.
 *
 * @author Christoph Strobl
 * @author Mark Paluch
 * @author Peter-Josef Meisch
 * @since 3.2
 */
class SingleNodeHostProvider implements HostProvider<com.lizhen.elasticsearch.client.reactive.SingleNodeHostProvider> {

	private final WebClientProvider clientProvider;
	private final Supplier<HttpHeaders> headersSupplier;
	private final InetSocketAddress endpoint;
	private volatile ElasticsearchHost state;

	SingleNodeHostProvider(WebClientProvider clientProvider, Supplier<HttpHeaders> headersSupplier,
			InetSocketAddress endpoint) {

		this.clientProvider = clientProvider;
		this.headersSupplier = headersSupplier;
		this.endpoint = endpoint;
		this.state = new ElasticsearchHost(this.endpoint, State.UNKNOWN);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lizhen.elasticsearch.client.reactive.HostProvider#clusterInfo()
	 */
	@Override
	public Mono<ClusterInformation> clusterInfo() {

		return createWebClient(endpoint) //
				.head().uri("/") //
				.headers(httpHeaders -> httpHeaders.addAll(headersSupplier.get())) //
				.exchangeToMono(it -> {
					if (it.statusCode().isError()) {
						state = ElasticsearchHost.offline(endpoint);
					} else {
						state = ElasticsearchHost.online(endpoint);
					}
					return Mono.just(state);
				}).onErrorResume(throwable -> {
					state = ElasticsearchHost.offline(endpoint);
					clientProvider.getErrorListener().accept(throwable);
					return Mono.just(state);
				}).map(elasticsearchHost -> new ClusterInformation(Collections.singleton(elasticsearchHost)));
	}

	/*
	 * (non-Javadoc)
	 * @see com.lizhen.elasticsearch.client.reactive.HostProvider#createWebClient(java.net.InetSocketAddress)
	 */
	@Override
	public WebClient createWebClient(InetSocketAddress endpoint) {
		return this.clientProvider.get(endpoint);
	}

	/*
	 * (non-Javadoc)
	 * @see com.lizhen.elasticsearch.client.reactive.HostProvider#lookupActiveHost(com.lizhen.elasticsearch.client.reactive.HostProvider.Verification)
	 */
	@Override
	public Mono<InetSocketAddress> lookupActiveHost(Verification verification) {

		if (Verification.LAZY.equals(verification) && state.isOnline()) {
			return Mono.just(endpoint);
		}

		return clusterInfo().handle((information, sink) -> {

			ElasticsearchHost host = information.getNodes().iterator().next();
			if (host.isOnline()) {

				sink.next(host.getEndpoint());
				return;
			}

			sink.error(new NoReachableHostException(Collections.singleton(host)));
		});
	}

	ElasticsearchHost getCachedHostState() {
		return state;
	}
}
