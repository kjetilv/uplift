package uplift.asynchttp;

public record WritableBuffer<B>(
    B buffer,
    int size
) implements Writable<B> {

}
