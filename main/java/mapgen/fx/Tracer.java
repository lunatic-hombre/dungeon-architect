package mapgen.fx;

public interface Tracer<D, E> {

    Tracer then(double distance, D direction);

    E get();

}
