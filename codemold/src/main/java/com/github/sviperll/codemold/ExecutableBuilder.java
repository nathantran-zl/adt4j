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
import com.github.sviperll.codemold.util.Collections2;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <T>
 * @param <D>
 */
@ParametersAreNonnullByDefault
public abstract class ExecutableBuilder<T extends ExecutableType<T, D>, D extends ExecutableDefinition<T, D>>
        extends GenericDefinitionBuilder<NestingBuilder, T, D> {
    private final VariableScope scope = VariableScope.createTopLevel();
    private final List<VariableDeclaration> parameters = Collections2.newArrayList();
    private final List<AnyType> throwsList = Collections2.newArrayList();
    private final NestingBuilder residence;
    private BlockBuilder body = null;

    ExecutableBuilder(NestingBuilder residence) {
        super(residence);
        this.residence = residence;
        
    }

    public void setAccessLevel(MemberAccess accessLevel) {
        residence.setAccessLevel(accessLevel);
    }

    @Override
    public final D definition() {
        return super.definition();
    }

    @Nonnull
    abstract D createDefinition(ExecutableDefinition.Implementation<T, D> implementation);

    @Override
    final D createDefinition(TypeParameters typeParameters) {
        return createDefinition(new BuiltExecutableDefinition(typeParameters));
    }

    @Override
    public TypeParameterBuilder typeParameter(String name) throws CodeMoldException {
        return super.typeParameter(name);
    }

    public VariableDeclaration addParameter(Type type, String name) throws CodeMoldException {
        name = scope.makeIntroducable(name);
        scope.introduce(name);
        Parameter parameter = new Parameter(false, type.asAny(), name);
        parameters.add(parameter);
        return parameter;
    }

    public VariableDeclaration addFinalParameter(Type type, String name) throws CodeMoldException {
        name = scope.makeIntroducable(name);
        scope.introduce(name);
        Parameter parameter = new Parameter(true, type.asAny(), name);
        parameters.add(parameter);
        return parameter;
    }

    public void throwsException(ObjectType type) throws CodeMoldException {
        if (type.definition().isGeneric())
            throw new CodeMoldException("Generic class can't be used as throwable exception");
        throwsList.add(type.asAny());
    }

    public void throwsException(TypeVariable typeVariable) throws CodeMoldException {
        throwsList.add(typeVariable.asAny());
    }

    @Nonnull
    public BlockBuilder body() {
        if (body == null) {
            Residence expressionContext = new MethodLocalResidence(definition()).residence();
            body = BlockBuilder.createWithBracesForced(new ExpressionContextDefinition(expressionContext), scope.createNested());
        }
        return body;
    }

    private class BuiltExecutableDefinition implements ExecutableDefinition.Implementation<T, D> {

        private final TypeParameters typeParameters;
        BuiltExecutableDefinition(TypeParameters typeParameters) {
            this.typeParameters = typeParameters;
        }
        @Override
        public final List<? extends VariableDeclaration> parameters() {
            return Snapshot.of(parameters);
        }

        @Override
        public final List<? extends AnyType> throwsList() {
            return Snapshot.of(throwsList);
        }

        @Override
        public final Renderable body() {
            return body;
        }

        @Override
        public final Nesting nesting() {
            return residence.residence().getNesting();
        }

        @Override
        public TypeParameters typeParameters(ExecutableDefinition<T, D> thisDefinition) {
            return typeParameters;
        }

    }

    private static class Parameter extends VariableDeclaration {

        private final boolean isFinal;
        private final AnyType type;
        private final String name;

        Parameter(boolean isFinal, AnyType type, String name) throws CodeMoldException {
            if (!(type.isArray() || type.isObjectType() || type.isPrimitive() || type.isTypeVariable()))
                throw new CodeMoldException(type.kind() + " is not allowed here");
            this.isFinal = isFinal;
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Override
        public AnyType type() {
            return type;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean isInitialized() {
            return false;
        }

        @Override
        Renderable getInitialValue() {
            throw new UnsupportedOperationException();
        }
    }

}