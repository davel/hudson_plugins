package com.ikokoon.instrumentation.dependency;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.ikokoon.instrumentation.Collector;
import com.ikokoon.toolkit.Toolkit;

/**
 * Please @see DependencyClassAdapter for more on dependency.
 * 
 * @author Michael Couck
 * @since 18.07.09
 * @version 01.00
 */
public class DependencyMethodAdapter extends MethodAdapter implements Opcodes {

	/** The logger for the class. */
	private Logger logger = Logger.getLogger(DependencyMethodAdapter.class);
	/** The name of the class that this method adapter is enhancing the methods for. */
	private String className;

	/**
	 * The constructor takes all the interesting items for the method that is to be enhanced.
	 * 
	 * @param methodVisitor
	 *            the method visitor of the parent
	 * @param className
	 *            the name of the class the method belongs to
	 * @param access
	 *            the access code for the method
	 * @param name
	 *            the name of the method
	 * @param desc
	 *            the description of the method
	 * @param exceptions
	 *            exceptions that can be thrown by the method
	 */
	public DependencyMethodAdapter(MethodVisitor methodVisitor, String className, String methodName, String methodDescription) {
		super(methodVisitor);
		this.className = className;
		String[] methodClasses = Toolkit.byteCodeSignatureToClassNameArray(methodDescription);
		Collector.collectMetrics(className, methodClasses);
		if (logger.isDebugEnabled()) {
			logger.debug("Class name : " + className + ", name : " + methodName + ", desc : " + methodDescription);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		String[] annotationClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, annotationClasses);
		return super.visitAnnotation(desc, visible);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitFieldInst - " + owner + ", " + name + ", " + desc);
		}
		String[] fieldClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, fieldClasses);
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitLocalVariable - " + name + ", " + desc + ", " + signature + ", " + start + ", " + end + ", " + index);
		}
		String[] variableClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, variableClasses);
		variableClasses = Toolkit.byteCodeSignatureToClassNameArray(signature);
		Collector.collectMetrics(className, variableClasses);
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMethodInst - " + opcode + ", " + owner + ", " + name + ", " + desc);
		}
		String[] methodClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, methodClasses);
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visitMultiANewArrayInsn(String desc, int dims) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitMultiANewArrayInst - " + desc + ", " + dims);
		}
		String[] arrayClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, arrayClasses);
		super.visitMultiANewArrayInsn(desc, dims);
	}

	/**
	 * {@inheritDoc}
	 */
	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		if (logger.isDebugEnabled()) {
			logger.debug("visitParameterAnnotation - " + parameter + ", " + desc + ", " + visible);
		}
		String[] parameterAnnotationClasses = Toolkit.byteCodeSignatureToClassNameArray(desc);
		Collector.collectMetrics(className, parameterAnnotationClasses);
		return super.visitParameterAnnotation(parameter, desc, visible);
	}

}