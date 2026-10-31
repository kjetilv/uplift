package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

/// Renders types as Java source. Type annotations are dropped, and declared types are fully qualified.
final class SourceTypes extends SimpleTypeVisitor14<String, Void> {

    static String render(TypeMirror type) {
        return type.accept(INSTANCE, null);
    }

    private static final SourceTypes INSTANCE = new SourceTypes();

    private SourceTypes() {
    }

    @Override
    public String visitPrimitive(PrimitiveType type, Void unused) {
        return type.getKind().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String visitArray(ArrayType type, Void unused) {
        return render(type.getComponentType()) + "[]";
    }

    @Override
    public String visitDeclared(DeclaredType type, Void unused) {
        var name = ((QualifiedNameable) type.asElement()).getQualifiedName().toString();
        var arguments = type.getTypeArguments();
        return arguments.isEmpty()
            ? name
            : name + arguments.stream()
                .map(SourceTypes::render)
                .collect(Collectors.joining(", ", "<", ">"));
    }

    @Override
    public String visitWildcard(WildcardType type, Void unused) {
        return Optional.ofNullable(type.getExtendsBound())
            .map(bound -> "? extends " + render(bound))
            .or(() -> Optional.ofNullable(type.getSuperBound())
                .map(bound -> "? super " + render(bound)))
            .orElse("?");
    }

    @Override
    public String visitTypeVariable(TypeVariable type, Void unused) {
        return type.asElement().getSimpleName().toString();
    }

    @Override
    protected String defaultAction(TypeMirror type, Void unused) {
        throw new IllegalArgumentException("Cannot render type of kind " + type.getKind() + ": " + type);
    }
}
