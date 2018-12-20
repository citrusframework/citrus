/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.types;

/**
 * Very basic tuple implementation. No support for equals or hashcode so be careful when using this in collections
 */
public class Tuple<A, B> {
    public final A _1;
    public final B _2;

    public static <A, B> Tuple<A, B> createTuple(A _1, B _2) {
        return new Tuple<>(_1, _2);
    }

    private Tuple(A _1, B _2) {
        this._1 = _1;
        this._2 = _2;
    }
}