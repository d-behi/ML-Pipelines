/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.streaming.sampling.samplers;

import org.apache.flink.api.common.functions.Function;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by marthavk on 2015-04-07.
 */
public interface SampleFunction<T> extends Function, Serializable {

	/**
	 * @return an ArrayList holding the whole sample
	 */
	public ArrayList<T> getElements();

	/**
	 * In a streaming fashion a sampler receives individual elements and puts them in its sample.
	 * The sample method should include the core sample creation logic of a stream sampler.
	 *
	 * @param element
	 */
	public void sample(T element);

	/**
	 * Produces a random output from the buffer
	 *
	 * @return
	 */
	public T getRandomEvent() throws IndexOutOfBoundsException;

	/**
	 * Resets the buffer
	 */
	public void reset();

	/**
	 * retrieves tha sample rate of the sampler (records per second)
	 */
	public double getSampleRate();

	/**
	 * [DEBUG] filename for writing output
	 */
	public String getFilename();


}