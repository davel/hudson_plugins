package com.schneide.crap4j.crap;

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
		if (hasCrapDataFor(data.getContext())) {
			System.err.println("Already registered crap for: " + data.getContext()); //$NON-NLS-1$
		}
		this.methodCrapData.put(data.getContext(), data);
	}

	public boolean hasCrapDataFor(IMethod method) {
		return this.methodCrapData.containsKey(method);
	}

	public IMethodCrapData getCrapDataFor(IMethod method) {
		return this.methodCrapData.get(method);
	}

	public GenericCrapData<IMethod> getGenericCrapDataFor(IMethod method) {
		IMethodCrapData crapData = getCrapDataFor(method);
		return new GenericCrapData<IMethod>(method, crapData);
	}

	public IMethodCrapData[] getAllCrapData() {
		return this.methodCrapData.values().toArray(new IMethodCrapData[this.methodCrapData.size()]);
	}
}
