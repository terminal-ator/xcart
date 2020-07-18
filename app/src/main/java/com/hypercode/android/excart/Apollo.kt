package com.hypercode.android.excart

import com.apollographql.apollo.ApolloClient

const val SERVER_URL = "http://192.168.0.15:4000"

val apolloClient = ApolloClient.builder().serverUrl(SERVER_URL)
                            .build()
