/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.model.selector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.knowledge.NeighborProviderIndex;
import software.amazon.smithy.model.shapes.Shape;

/**
 * Provides a toString method that prints the expression.
 */
final class WrappedSelector implements Selector {

    /** Uses parallel streams when the model size exceeds this number. */
    private static final int PARALLEL_THRESHOLD = 10000;

    private final String expression;
    private final InternalSelector delegate;
    private final Function<Model, Collection<? extends Shape>> optimizer;

    WrappedSelector(String expression, List<InternalSelector> selectors) {
        this.expression = expression;
        delegate = AndSelector.of(selectors);
        optimizer = selectors.get(0).optimize();
    }

    @Override
    public String toString() {
        return expression;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Selector && toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }

    @Override
    public Set<Shape> select(Model model) {
        if (isParallel(model)) {
            return shapes(model).collect(Collectors.toSet());
        } else {
            Set<Shape> result = new HashSet<>();
            // This is more optimized than using shapes() for smaller models
            // that aren't parallelized.
            pushShapes(model, (ctx, s) -> {
                result.add(s);
                return true;
            });
            return result;
        }
    }

    @Override
    public void consumeMatches(Model model, Consumer<ShapeMatch> shapeMatchConsumer) {
        // This is more optimized than using matches() and collecting to a Set
        // because it avoids creating streams and buffering the result of
        // pushing each shape into internal selectors.
        pushShapes(model, (ctx, s) -> {
            shapeMatchConsumer.accept(new ShapeMatch(s, ctx.getVars()));
            return true;
        });
    }

    @Override
    public Stream<Shape> shapes(Model model) {
        return streamStartingShape(model).flatMap(shape -> {
            List<Shape> result = new ArrayList<>();
            delegate.push(createContext(model), shape, (ctx, s) -> {
                result.add(s);
                return true;
            });
            return result.stream();
        });
    }

    @Override
    public Stream<ShapeMatch> matches(Model model) {
        return streamStartingShape(model).flatMap(shape -> {
            List<ShapeMatch> result = new ArrayList<>();
            delegate.push(createContext(model), shape, (ctx, s) -> {
                result.add(new ShapeMatch(s, ctx.getVars()));
                return true;
            });
            return result.stream();
        });
    }

    private Context createContext(Model model) {
        return new Context(NeighborProviderIndex.of(model));
    }

    private void pushShapes(Model model, InternalSelector.Receiver acceptor) {
        Context context = createContext(model);
        Collection<? extends Shape> shapes = optimizer == null
                ? model.toSet()
                : optimizer.apply(model);
        for (Shape shape : shapes) {
            delegate.push(context.clearVars(), shape, acceptor);
        }
    }

    private Stream<? extends Shape> streamStartingShape(Model model) {
        Stream<? extends Shape> stream = optimizer != null
                ? optimizer.apply(model).stream()
                : model.shapes();

        // Use a parallel stream for larger models.
        if (isParallel(model)) {
            stream = stream.parallel();
        }

        return stream;
    }

    private boolean isParallel(Model model) {
        return model.getShapeIds().size() >= PARALLEL_THRESHOLD;
    }
}
