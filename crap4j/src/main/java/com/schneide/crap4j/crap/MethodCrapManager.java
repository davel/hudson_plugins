package com.schneide.crap4j.crap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.schneide.crap4j.reader.model.IMethod;
import com.schneide.crap4j.reader.model.IMethodCrapData;

public class MethodCrapManager {

	private final Map<IMethod, IMethodCrapData> methodCrapData;

	public MethodCrapManager() {
		super();
		this.methodCrapData = new HashMap<IMethod, IMethodCrapData>();
	}

	public void addMethodCrapData(IMethodCrapData data) {
		if (!data.isCrappy()) {
			return;
		}
		if (hasCrapDataFor(data.getContext())) {
			System.err.println("Already registered crap for: " + data.getContext()); //$NON-NLS-1$
		}
		this.methodCrapData.put(data.getContext(), data);
	}

	protected boolean hasCrapDataFor(IMethod method) {
		return this.methodCrapData.containsKey(method);
	}

	public Collection<IMethodCrapData> getCrapData() {
		return Collections.unmodifiableCollection(this.methodCrapData.values());
	}
}
