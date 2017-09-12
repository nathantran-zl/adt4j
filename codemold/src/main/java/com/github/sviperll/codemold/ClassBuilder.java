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

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
public class ClassBuilder<B extends ResidenceProvider>
        extends AbstractClassBuilder<B> {
    private ObjectType extendsClass = null;
    private boolean isFinal = false;

    public ClassBuilder(B residence, String name) {
        super(ObjectKind.CLASS, residence, name);
    }

    @Nonnull
    @Override
    public TypeParameterBuilder typeParameter(String name) throws CodeMoldException {
        return super.typeParameter(name);
    }

    public void setFinal(boolean value) {
        this.isFinal = value;
    }

    public void extendsClass(ObjectType type) throws CodeMoldException {
        if (this.extendsClass != null) {
            throw new CodeMoldException("Already extended");
        }
        if (!type.definition().kind().isClass()) {
            throw new CodeMoldException("Only classes can be extended");
        }
        if (!type.definition().isFinal()) {
            throw new CodeMoldException("Trying to extend final class");
        }
        if (type.containsWildcards()) {
            throw new CodeMoldException("Wildcards are not allowed in extends clause");
        }
        this.extendsClass = type;
    }

    @Nonnull
    @Override
    ObjectDefinition createDefinition(TypeParameters typeParameters) {
        return new BuiltDefinition(typeParameters);
    }

    private class BuiltDefinition extends AbstractClassBuilder<B>.BuiltDefinition {

        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Nonnull
        @Override
        final public ObjectType extendsClass() {
            return extendsClass != null ? extendsClass : getCodeMold().objectType();
        }

        @Override
        public boolean isFinal() {
            return isFinal;
        }

        @Nonnull
        @Override
        public List<? extends EnumConstant> enumConstants() {
            throw new UnsupportedOperationException("Enum constants are listed for enum definitions only. Use kind() method to check for object kind.");
        }

    }
}
