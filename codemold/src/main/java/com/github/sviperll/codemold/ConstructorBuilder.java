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

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class ConstructorBuilder extends ExecutableBuilder<ConstructorType, ConstructorDefinition> {
    ConstructorBuilder(NestingBuilder residence) throws CodeMoldException {
        super(residence);
        if (residence.residence().getNesting().isStatic())
            throw new CodeMoldException("Constructor can't be static");
    }

    @Nonnull
    @Override
    ConstructorDefinition createDefinition(ExecutableDefinition.Implementation<ConstructorType, ConstructorDefinition> implementation) {
        return new BuiltDefinition(implementation);
    }

    @Nonnull
    @Override
    public TypeParameterBuilder typeParameter(String name) throws CodeMoldException {
        return super.typeParameter(name);
    }

    @Nonnull
    @Override
    public VariableDeclaration addParameter(Type type, String name) throws CodeMoldException {
        return super.addParameter(type, name);
    }

    @Nonnull
    @Override
    public VariableDeclaration addFinalParameter(Type type, String name) throws CodeMoldException {
        return super.addFinalParameter(type, name);
    }

    @Override
    public void throwsException(ObjectType type) throws CodeMoldException {
        super.throwsException(type);
    }

    @Override
    public void throwsException(TypeVariable typeVariable) throws CodeMoldException {
        super.throwsException(typeVariable);
    }

    @Nonnull
    @Override
    public BlockBuilder body() {
        return super.body();
    }

    private static class BuiltDefinition extends ConstructorDefinition {
        BuiltDefinition(ExecutableDefinition.Implementation<ConstructorType, ConstructorDefinition> implementation) {
            super(implementation);
        }
    }

}
