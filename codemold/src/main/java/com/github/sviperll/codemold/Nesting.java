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
import javax.annotation.Nonnull;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
public abstract class Nesting implements Renderable, ResidenceProvider {
    private Residence residence = null;
    private final RenderableNesting defaultRenderable = new RenderableNesting(false);
    private final RenderableNesting implicitlyStaticRenderable = new RenderableNesting(true);
    Nesting() {
    }

    @Nonnull
    public abstract MemberAccess accessLevel();

    public abstract boolean isStatic();

    @Nonnull
    public abstract ObjectDefinition parent();

    @Override
    public final Residence residence() {
        if (residence == null)
            residence = Residence.wrapNested(new Wrappable());
        return residence;
    }

    @Nonnull
    @Override
    public Renderer createRenderer(final RendererContext context) {
        return defaultRenderable.createRenderer(context);
    }

    Renderable forObjectKind(ObjectKind kind) {
        if (kind.implicitlyStatic())
            return implicitlyStaticRenderable;
        else
            return defaultRenderable;
    }

    class Wrappable {
        private Wrappable() {
        }
        Nesting value() {
            return Nesting.this;
        }
    }

    class RenderableNesting implements Renderable {
        private final boolean implicitlyStatic;

        RenderableNesting(boolean implicitlyStatic) {
            this.implicitlyStatic = implicitlyStatic;
        }

        @Nonnull
        @Override
        public Renderer createRenderer(final RendererContext context) {
            return () -> {
                context.appendRenderable(accessLevel());
                context.appendWhiteSpace();
                if (isStatic() && !implicitlyStatic)
                    context.appendText("static");
            };
        }
    }
}
