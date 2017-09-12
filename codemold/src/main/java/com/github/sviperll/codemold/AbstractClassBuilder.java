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

import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
abstract class AbstractClassBuilder<B extends ResidenceProvider>
        extends NamedObjectBuilder<B, MethodBuilder> {

    private final List<ObjectType> interfaces = CMCollections.newArrayList();
    private final List<ConstructorDefinition> constructors = CMCollections.newArrayList();

    AbstractClassBuilder(ObjectKind kind, B residence, String name) {
        super(kind, residence, name);
    }

    @Nonnull
    @Override
    public FieldBuilder field(Type type, String name) throws CodeMoldException {
        return super.field(type, name);
    }

    @Nonnull
    @Override
    public FieldBuilder finalField(Type type, String name) throws CodeMoldException {
        return super.finalField(type, name);
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
    public ClassBuilder<NestingBuilder> innerClass(String name) throws CodeMoldException {
        return super.innerClass(name);
    }

    @Nonnull
    @Override
    public MethodBuilder method(String name) throws CodeMoldException {
        return super.method(name);
    }

    @Nonnull
    @Override
    public MethodBuilder staticMethod(String name) throws CodeMoldException {
        return super.staticMethod(name);
    }

    @Nonnull
    @Override
    MethodBuilder createMethodBuilder(NestingBuilder methodResidence, String name) {
        return new MethodBuilder(methodResidence, name);
    }

    public void implementsInterface(ObjectType type) throws CodeMoldException {
        if (!type.definition().kind().isInterface()) {
            throw new CodeMoldException("Only interfaces can be implemented");
        }
        if (type.containsWildcards()) {
            throw new CodeMoldException("Wildcards are not allowed in implemenents clause");
        }
        interfaces.add(type);
    }

    public ConstructorBuilder addConstructor() throws CodeMoldException {
        NestingBuilder methodResidence = new NestingBuilder(false, definition());
        ConstructorBuilder result = new ConstructorBuilder(methodResidence);
        constructors.add(result.definition());
        return result;
    }

    abstract class BuiltDefinition extends NamedObjectBuilder<B, MethodBuilder>.BuiltDefinition {

        BuiltDefinition(TypeParameters typeParameters) {
            super(typeParameters);
        }

        @Nonnull
        @Override
        final public List<? extends ObjectType> implementsInterfaces() {
            return Snapshot.of(interfaces);
        }

        @Nonnull
        @Override
        final public List<? extends ConstructorDefinition> constructors() {
            return Snapshot.of(constructors);
        }
    }

}
