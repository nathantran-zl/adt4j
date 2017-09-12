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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public abstract class ObjectDefinition
        extends GenericDefinition<ObjectType, ObjectDefinition>
        implements Annotated {
    private static final Logger logger = Logger.getLogger(ObjectDefinition.class.getName());

    ObjectDefinition() {
    }

    @Nonnull
    @Override
    public abstract Residence residence();

    public abstract boolean isFinal();

    @Nonnull
    public abstract ObjectKind kind();

    @Nonnull
    public abstract ObjectType extendsClass();

    @Nonnull
    public abstract List<? extends ObjectType> implementsInterfaces();

    @Nonnull
    public abstract List<? extends EnumConstant> enumConstants();

    @Nonnull
    public abstract List<? extends ConstructorDefinition> constructors();

    @Nonnull
    public abstract List<? extends MethodDefinition> methods();

    @Nonnull
    public abstract Collection<? extends ObjectDefinition> innerClasses();

    @Nonnull
    public abstract Collection<? extends FieldDeclaration> fields();

    /**
     * Class' simple name.
     * Throws UnsupportedOperationException for anonymous classes.
     * @see ObjectDefinition#isAnonymous()
     * @throws UnsupportedOperationException for anonymous classes.
     * @return class' simple name
     */
    @Nonnull
    public abstract String simpleTypeName();

    public abstract boolean isAnonymous();

    @Nonnull
    abstract List<? extends Renderable> staticInitializationElements();

    @Nonnull
    abstract List<? extends Renderable> instanceInitializationElements();

    @Nonnull
    public final String qualifiedTypeName() {
        return residence().getPackage().qualifiedName() + "." + simpleTypeName();
    }

    public final boolean isJavaLangObject() {
        return this == getCodeMold().objectType().definition();
    }

    public boolean declaresConstructors() {
        return !isAnonymous() && kind().declaresConstructors();
    }

    public Expression classLiteral() {
        return Expression.classLiteral(this);
    }

    @Nonnull
    @Override
    final ObjectType createType(GenericType.Implementation<ObjectType, ObjectDefinition> implementation) {
        return new ObjectType(implementation);
    }

    @Nonnull
    @Override
    final ObjectDefinition fromGenericDefinition() {
        return this;
    }

    public final boolean extendsOrImplements(ObjectDefinition objectDefinition) {
        return this == objectDefinition
               || this.extendsClass().definition().extendsOrImplements(objectDefinition)
               || implementsInterfaces().stream().map(ObjectType::definition).anyMatch(iface -> iface.extendsOrImplements(objectDefinition));
    }

    final Optional<ObjectDefinition> getReference(String relativelyQualifiedName) {
        int index = relativelyQualifiedName.indexOf('.');
        if (index == 0)
            throw new IllegalArgumentException(relativelyQualifiedName + " illegal name");
        boolean needsToGoDeeper = index >= 0;
        String simpleName = !needsToGoDeeper ? relativelyQualifiedName : relativelyQualifiedName.substring(0, index);
        for (ObjectDefinition innerClass: innerClasses()) {
            if (innerClass.simpleTypeName().equals(simpleName)) {
                if (!needsToGoDeeper)
                    return Optional.of(innerClass);
                else
                    return innerClass.getReference(relativelyQualifiedName.substring(simpleName.length() + 1));
            }
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public final Renderer createRenderer(final RendererContext context) {
        if (isJavaLangObject())
            throw new IllegalStateException("java.lang.Object class definition is not renderable");
        return () -> {
            if (!isAnonymous()) {
                allAnnotations().forEach(annotation -> {
                    context.appendRenderable(annotation);
                    context.appendLineBreak();
                });
                context.appendRenderable(residence().forObjectKind(kind()));
                context.appendWhiteSpace();
                if (!kind().implicitlyFinal() && isFinal())
                    context.appendText("final");
                context.appendWhiteSpace();
                context.appendRenderable(kind());
                context.appendWhiteSpace();
                context.appendText(simpleTypeName());
                context.appendRenderable(typeParameters());
                if (kind().extendsSomeClass() && !extendsClass().isJavaLangObject()) {
                    context.appendText(" extends ");
                    context.appendRenderable(extendsClass());
                }
                if (kind().implementsSomeInterfaces()) {
                    Iterator<? extends ObjectType> interfaces = implementsInterfaces().iterator();
                    if (interfaces.hasNext()) {
                        if (kind().isInterface())
                            context.appendText(" extends ");
                        else
                            context.appendText(" implements ");
                        ObjectType implementedInterface = interfaces.next();
                        context.appendRenderable(implementedInterface);
                        while (interfaces.hasNext()) {
                            context.appendText(", ");
                            implementedInterface = interfaces.next();
                            context.appendRenderable(implementedInterface);
                        }
                    }
                }
                context.appendWhiteSpace();
            }
            context.appendText("{");
            context.appendLineBreak();
            RendererContext nestedContext = context.indented();

            if (kind().isEnum()) {
                Iterator<? extends EnumConstant> iterator = enumConstants().iterator();
                nestedContext.appendRenderable(iterator.next().definition());
                while (iterator.hasNext()) {
                    nestedContext.appendText(", ");
                    nestedContext.appendRenderable(iterator.next().definition());
                }
                nestedContext.appendText(";");
                nestedContext.appendLineBreak();
            }

            staticInitializationElements().stream().forEach((element) -> {
                nestedContext.appendRenderable(element);
            });

            methods().stream().filter(MethodDefinition::isStatic).forEach((method) -> {
                nestedContext.appendEmptyLine();
                nestedContext.appendRenderable(method);
            });

            instanceInitializationElements().stream().forEach((element) -> {
                nestedContext.appendRenderable(element);
            });

            if (declaresConstructors()) {
                constructors().stream().forEach((constructor) -> {
                    nestedContext.appendEmptyLine();
                    nestedContext.appendRenderable(constructor);
                });
            }

            methods().stream().filter(method -> !method.isStatic()).forEach((method) -> {
                nestedContext.appendEmptyLine();
                nestedContext.appendRenderable(method);
            });

            innerClasses().stream().filter(innerClass -> !innerClass.residence().getNesting().isStatic()).forEach((innerClass) -> {
                nestedContext.appendEmptyLine();
                nestedContext.appendRenderable(innerClass);
            });

            innerClasses().stream().filter(innerClass -> innerClass.residence().getNesting().isStatic()).forEach((innerClass) -> {
                nestedContext.appendEmptyLine();
                nestedContext.appendRenderable(innerClass);
            });

            context.appendText("}");
        };
    }
}
