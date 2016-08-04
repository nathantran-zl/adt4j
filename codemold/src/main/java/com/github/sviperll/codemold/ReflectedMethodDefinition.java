/*
 * Copyright (c) 2016, Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.sviperll.codemold;

import com.github.sviperll.codemold.render.Renderable;
import com.github.sviperll.codemold.render.Renderer;
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
 class ReflectedMethodDefinition extends MethodDefinition {

    static ReflectedMethodDefinition createInstance(Reflection reflection, Nesting nesting, Method method) {
        ReflectedExecutableDefinitionImplementation executable = new ReflectedExecutableDefinitionImplementation(reflection, nesting, method);
        return executable.definition();
    }
    private final Reflection reflection;
    private final Method method;
    private AnyType returnType = null;

    ReflectedMethodDefinition(Reflection reflection, ReflectedExecutableDefinitionImplementation executable, Method method) {
        super(executable);
        this.reflection = reflection;
        this.method = method;
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }

    @Override
    public AnyType returnType() {
        if (returnType == null) {
            returnType = reflection.readReflectedType(method.getGenericReturnType());
        }
        return returnType;
    }

    @Override
    public String name() {
        return method.getName();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean hasDefaultValue() {
        return method.getDefaultValue() != null;
    }

    @Override
    public AnyCompileTimeValue defaultValue() {
        if (!hasDefaultValue()) {
            throw new UnsupportedOperationException("No default value");
        }
        return CompileTimeValues.fromObject(method.getDefaultValue());
    }

    private static class ReflectedExecutableDefinitionImplementation implements ExecutableDefinition.Implementation<MethodType, MethodDefinition> {
        private final Reflection reflection;
        private final Nesting nesting;
        private final Method method;
        private ReflectedMethodDefinition definition = null;
        private List<? extends VariableDeclaration> parameters = null;
        private List<? extends AnyType> throwsList = null;
        private AnnotationCollection annotations = null;
        private TypeParameters typeParameters = null;

        private ReflectedExecutableDefinitionImplementation(Reflection reflection, Nesting nesting, Method method) {
            this.reflection = reflection;
            this.nesting = nesting;
            this.method = method;
        }

        ReflectedMethodDefinition definition() {
            if (definition == null) {
                definition = new ReflectedMethodDefinition(reflection, this, method);
            }
            return definition;
        }

        @Override
        public TypeParameters typeParameters() {
            if (typeParameters == null) {
                typeParameters = new ReflectedTypeParameters<>(reflection, definition(), method.getTypeParameters());
            }
            return typeParameters;
        }

        @Override
        public List<? extends VariableDeclaration> parameters() {
            if (parameters == null) {
                parameters = Snapshot.of(reflection.createParameterList(method.getParameters()));
            }
            return Snapshot.of(parameters);
        }

        @Override
        public List<? extends AnyType> throwsList() {
            if (throwsList == null) {
                throwsList = Snapshot.of(reflection.buildTypesFromReflections(method.getGenericExceptionTypes()));
            }
            return Snapshot.of(throwsList);
        }

        @Override
        public Renderable body() {
            return Reflection.renderableUnaccessibleCode();
        }

        @Override
        public Nesting nesting() {
            return nesting;
        }

        @Override
        public List<? extends Annotation> getAnnotation(ObjectDefinition definition) {
            initAnnotations();
            return annotations.getAnnotation(definition);
        }

        @Override
        public Collection<? extends Annotation> allAnnotations() {
            initAnnotations();
            return annotations.allAnnotations();
        }

        private void initAnnotations() {
            if (annotations == null) {
                annotations = reflection.readAnnotationCollection(method.getDeclaredAnnotations());
            }
        }
    }


}