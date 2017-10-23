package com.ontimize.jee.server.hessian;

import org.springframework.remoting.caucho.HessianServiceExporter;

import com.caucho.hessian.server.HessianSkeleton;
import com.caucho.hessian.util.IExceptionTranslator;
import com.ontimize.jee.common.tools.ReflectionTools;

public class OntimizeHessianExporter extends HessianServiceExporter {

	private IExceptionTranslator exceptionTranslator;

	public OntimizeHessianExporter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.remoting.caucho.HessianExporter#setDebug(boolean)
	 */
	@Override
	public void setDebug(boolean debug) {
		super.setDebug(debug);
	}

	@Override
	public void prepare() {
		this.checkService();
		this.checkServiceInterface();
		ReflectionTools.setFieldValue(this, "skeleton", new HessianSkeleton(this.getProxyForService(), this.getServiceInterface(), this.getExceptionTranslator()));
	}

	public IExceptionTranslator getExceptionTranslator() {
		return this.exceptionTranslator;
	}

	public void setExceptionTranslator(IExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}
}
