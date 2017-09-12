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
import com.github.sviperll.codemold.render.RendererContext;
import com.github.sviperll.codemold.util.CMCollections;
import com.github.sviperll.codemold.util.Snapshot;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 * @param <B>
 */
@ParametersAreNonnullByDefault
public class EnumBuilder<B extends ResidenceProvider> extends AbstractClassBuilder<B> {
    private static final NoOpConsumer NO_OP_CONSUMER = new NoOpConsumer();
    private final List<EnumConstant> constants = CMCollections.newArrayList();
    public EnumBuilder(B residence, String name) {
        super(ObjectKind.ENUM, residence, name);
    }

    public void constant(final String name, List<? extends Expression> constructorArguments, Consumer<? super AnonymousClassBuilder> customization) throws CodeMoldException {
        ObjectDefinition enumDefinition = definition();
        NestingBuilder nestingBuilder = new NestingBuilder(false, enumDefinition);
        nestingBuilder.setAccessLevel(MemberAccess.PRIVATE);
        AnonymousClassBuilder builder = new AnonymousClassBuilder(new ExpressionContextDefinition(nestingBuilder.residence()));
        customization.accept(builder);
        EnumConstant constant = new BuiltConstant(enumDefinition, name, constructorArguments, builder.definition());
        constants.add(constant);
    }

    public void constant(final String name, final List<? extends Expression> constructorArguments) throws CodeMoldException {
        constant(name, constructorArguments, NO_OP_CONSUMER);
    }

    public void constant(final String name, Consumer<? super AnonymousClassBuilder> customization) throws CodeMoldException {
        constant(name, Collections.<Expression>emptyList(), customization);
    }

    public void constant(String name) throws CodeMoldException {
        constant(name, Collections.<Expression>emptyList());
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

        @Override
        public boolean isFinal() {
            return true;
        }

        @Nonnull
        @Override
        public ObjectType extendsClass() {
            return getCodeMold().objectType();
        }

        @Nonnull
        @Override
        public List<? extends EnumConstant> enumConstants() {
            return Snapshot.of(constants);
        }
    }

    private static class BuiltConstant extends EnumConstant {
        private final ObjectDefinition enumDefinition;
        private final String name;
        private final List<? extends Expression> constructorArguments;
        private final ObjectDefinition members;
        BuiltConstant(ObjectDefinition enumDefinition, String name, List<? extends Expression> constructorArguments, ObjectDefinition members) {
            this.enumDefinition = enumDefinition;
            this.name = name;
            this.constructorArguments = Snapshot.of(constructorArguments);
            this.members = members;
        }


        @Override
        public ObjectDefinition enumDefinition() {
            return enumDefinition;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        Renderable definition() {
            return (RendererContext context) -> {
                return () -> {
                    context.appendText(name);
                    Iterator<? extends Expression> iterator = constructorArguments.iterator();
                    if (iterator.hasNext()) {
                        context.appendText("(");
                        context.appendRenderable(iterator.next());
                        while (iterator.hasNext()) {
                            context.appendText(", ");
                            context.appendRenderable(iterator.next());
                        }
                        context.appendText(")");
                    }
                    if (!(members.fields().isEmpty() && members.methods().isEmpty())) {
                        context.appendWhiteSpace();
                        context.appendRenderable(members);
                    }
                };
            };
        }

    }

    private static class NoOpConsumer implements Consumer<AnonymousClassBuilder> {

        NoOpConsumer() {
        }

        @Override
        public void accept(AnonymousClassBuilder value) {
        }
    }
}
