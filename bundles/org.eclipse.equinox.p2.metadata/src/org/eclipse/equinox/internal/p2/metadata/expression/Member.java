/*******************************************************************************
 * Copyright (c) 2009, 2021 Cloudsmith Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Cloudsmith Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.metadata.expression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.equinox.p2.metadata.expression.IEvaluationContext;
import org.eclipse.equinox.p2.metadata.expression.IExpressionVisitor;
import org.eclipse.equinox.p2.metadata.expression.IMemberProvider;

/**
 * <p>
 * An expression that performs member calls to obtain some value from some
 * object instance. It uses standard bean semantics so that an attempt to obtain
 * &quot;value&quot; will cause an attempt to call <code>getValue()</code> and
 * if no such method exists, <code>isValue()</code> and if that doesn't work
 * either, <code>value()</code>.
 * </p>
 */
public abstract class Member extends Unary {

	public static final class DynamicMember extends Member {
		private static final String GET_PREFIX = "get"; //$NON-NLS-1$
		private static final String IS_PREFIX = "is"; //$NON-NLS-1$

		private Class<?> lastClass;

		private transient Method method;
		private transient String methodName;

		DynamicMember(Expression operand, String name) {
			super(operand, name, Expression.emptyArray);
		}

		@Override
		public Object evaluate(IEvaluationContext context) {
			return invoke(operand.evaluate(context));
		}

		public final Object invoke(Object self) {
			if (self instanceof IMemberProvider)
				return ((IMemberProvider) self).getMember(name);

			if (self == null)
				throw new IllegalArgumentException("Cannot access member \'" + name + "\' in null"); //$NON-NLS-1$//$NON-NLS-2$

			Class<?> c = self.getClass();
			synchronized (this) {
				if (methodName == null) {
					String n = name;
					if (!(n.startsWith(GET_PREFIX) || n.startsWith(IS_PREFIX)))
						n = GET_PREFIX + Character.toUpperCase(n.charAt(0)) + n.substring(1);
					methodName = n;
				}
				if (lastClass == null || !lastClass.isAssignableFrom(c)) {
					Method m;
					for (;;) {
						try {
							m = c.getMethod(methodName);
							if (!Modifier.isPublic(m.getModifiers()))
								throw new NoSuchMethodException();
							break;
						} catch (NoSuchMethodException e) {
							if (methodName.startsWith(GET_PREFIX))
								// Switch from using getXxx() to isXxx()
								methodName = IS_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1);
							else if (methodName.startsWith(IS_PREFIX))
								// Switch from using isXxx() to xxx()
								methodName = name;
							else
								throw new IllegalArgumentException("Cannot find a public member \'" + name + "\' in a " //$NON-NLS-1$//$NON-NLS-2$
										+ self.getClass().getName());
						}
					}

					// Since we already checked that it's public. This will speed
					// up the calls a bit.
					m.setAccessible(true);
					lastClass = c;
					method = m;
				}

				Exception checked;
				try {
					return method.invoke(self);
				} catch (IllegalArgumentException e) {
					throw e;
				} catch (IllegalAccessException e) {
					checked = e;
				} catch (InvocationTargetException e) {
					Throwable cause = e.getTargetException();
					if (cause instanceof RuntimeException)
						throw (RuntimeException) cause;
					if (cause instanceof Error)
						throw (Error) cause;
					checked = (Exception) cause;
				}
				throw new RuntimeException("Problem invoking " + methodName + " on a " + self.getClass().getName(), //$NON-NLS-1$ //$NON-NLS-2$
						checked);
			}
		}
	}

	public static class LengthMember extends Member {

		LengthMember(Expression operand) {
			super(operand, "length", Expression.emptyArray); //$NON-NLS-1$
		}

		@Override
		public Object evaluate(IEvaluationContext context) {
			int len = getLength(operand.evaluate(context));
			return Integer.valueOf(len);
		}

		int getLength(Object val) {
			if (val == null)
				return 0;

			if (val.getClass().isArray())
				return java.lang.reflect.Array.getLength(val);

			if (val instanceof Collection<?>)
				return ((Collection<?>) val).size();

			if (val instanceof String)
				return ((String) val).length();

			if (val instanceof Map<?, ?>)
				return ((Map<?, ?>) val).size();

			return 0;
		}
	}

	public static class EmptyMember extends LengthMember {
		EmptyMember(Expression operand) {
			super(operand);
		}

		@Override
		public Object evaluate(IEvaluationContext context) {
			Object val = operand.evaluate(context);
			boolean empty = (val instanceof Iterator<?>) ? !((Iterator<?>) val).hasNext() : getLength(val) == 0;
			return Boolean.valueOf(empty);
		}
	}

	static Member createDynamicMember(Expression operand, String name) {
		return new DynamicMember(operand, name);
	}

	protected final Expression[] argExpressions;

	final String name;

	protected Member(Expression operand, String name, Expression[] args) {
		super(operand);
		this.name = name.intern();
		this.argExpressions = args;
	}

	@Override
	public boolean accept(IExpressionVisitor visitor) {
		if (super.accept(visitor))
			for (Expression expression : argExpressions)
				if (!expression.accept(visitor))
					return false;
		return true;
	}

	@Override
	public int compareTo(Expression e) {
		int cmp = super.compareTo(e);
		if (cmp == 0) {
			cmp = name.compareTo(((Member) e).name);
			if (cmp == 0)
				cmp = compare(argExpressions, ((Member) e).argExpressions);
		}
		return cmp;
	}

	@Override
	public boolean equals(Object o) {
		if (super.equals(o)) {
			Member mo = (Member) o;
			return name.equals(mo.name) && equals(argExpressions, mo.argExpressions);
		}
		return false;
	}

	@Override
	public int getExpressionType() {
		return TYPE_MEMBER;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getOperator() {
		return OPERATOR_MEMBER;
	}

	@Override
	public int getPriority() {
		return PRIORITY_MEMBER;
	}

	@Override
	public int hashCode() {
		int result = 31 + name.hashCode();
		result = 31 * result + operand.hashCode();
		return 31 * result + hashCode(argExpressions);
	}

	@Override
	public void toString(StringBuffer bld, Variable rootVariable) {
		if (operand != rootVariable) {
			appendOperand(bld, rootVariable, operand, getPriority());
			bld.append('.');
		}
		bld.append(name);
		if (argExpressions.length > 0) {
			bld.append('(');
			elementsToString(bld, rootVariable, argExpressions);
			bld.append(')');
		}
	}
}
