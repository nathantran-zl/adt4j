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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
public class AnnotationDefinitionBuilder<B extends ResidenceProvider>
        extends NamedObjectBuilder<B, AnnotationMethodBuilder> {

    public AnnotationDefinitionBuilder(B residence, String name) {
        super(ObjectKind.ANNOTATION, residence, name);
    }

    @Nonnull
    @Override
    public FieldBuilder staticField(Type type, String name) throws CodeMoldException {
        return super.staticField(type, name);
    }

    @Nonnull
    @Override
    public FieldBuilder staticFinalField(Type type, String name) throws CodeMoldException {
        return super.staticFinalField(type, name);
    }

    @Nonnull
    @Override
    public AnnotationMethodBuilder method(String name) throws CodeMoldException {
        return super.method(name);
    }

    @Nonnull
    @Override
    AnnotationMethodBuilder createMethodBuilder(NestingBuilder methodResidence, String name) {
        return new AnnotationMethodBuilder(methodResidence, name);
    }

    @Nonnull
    @Override
    ObjectDefinition createDefinition(TypeParameters typeParameters) {
        return new BuiltDefinition(typeParameters);
    }

    private class BuiltDefinition extends NamedObjectBuilder<B, MethodBuilder>.BuiltDefinition {

        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Nonnull
        @Override
        public ObjectType extendsClass() {
            return getCodeMold().objectType();
        }

        @Nonnull
        @Override
        public List<? extends ObjectType> implementsInterfaces() {
            return Collections.emptyList();
        }

        @Nonnull
        @Override
        public List<? extends ConstructorDefinition> constructors() {
            throw new UnsupportedOperationException("Constructors are listed for class definitions only. Use kind() method to check for object kind.");
        }

        @Nonnull
        @Override
        public List<? extends EnumConstant> enumConstants() {
            throw new UnsupportedOperationException("Enum constants are listed for enum definitions only. Use kind() method to check for object kind.");
        }

    }
}
