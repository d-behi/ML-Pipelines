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

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.incrementalML.inspector.PageHinkleyTest;
import org.apache.flink.streaming.sampling.helpers.SamplingUtils;
import org.apache.flink.streaming.sampling.helpers.StreamTimestamp;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by marthavk on 2015-05-11.
 * Greedy sampler uses a change detection component in order to detect concept drift
 * in the stream. If such a change is detected then a certain percentage of tuples
 * from the reservoir are evicted and new ones are sampled using the biased reservoir sampling
 * algorithm.
 */
public class GreedySampler<IN> implements MapFunction<IN, Sample<IN>>, Sampler<IN> {

	Reservoir reservoirSample;
	PageHinkleyTest detector;
	double lambda, delta;

	private boolean hasDrift =false;
	int count=0;

	public GreedySampler(int size) {
		reservoirSample = new Reservoir(size);
		Properties props = SamplingUtils.readProperties(SamplingUtils.path + "distributionconfig.properties");
		lambda = Double.parseDouble(props.getProperty("lambda"));
		delta = Double.parseDouble(props.getProperty("delta"));
		detector = new PageHinkleyTest(lambda, delta,30);
	}
	@Override
	public Sample<IN> map(IN value) throws Exception {
		count++;
		sample(value);
		return reservoirSample;
	}


	@Override
	public ArrayList<IN> getElements() {
		return reservoirSample.getSample();
	}

	@Override
	public void sample(IN element) {
		Tuple3 inValue = (Tuple3) element;
		detector.input(((Double) inValue.f0));
		hasDrift = detector.isChangedDetected();
		StreamTimestamp changeTimeStamp = new StreamTimestamp();
		System.out.println(changeTimeStamp.getTimestamp());

		if (hasDrift) {
			reservoirSample.discard(0.5);
			hasDrift = false;
			detector.reset();
		}

		double proportion = reservoirSample.getSize()/reservoirSample.getMaxSize();
		if (SamplingUtils.flip(proportion)) {
			reservoirSample.replaceSample(element);
		}
		else {
			reservoirSample.addSample(element);
		}
	}

	@Override
	public int size() {
		return reservoirSample.getSize();
	}

	@Override
	public int maxSize() {
		return reservoirSample.getMaxSize();
	}


}
