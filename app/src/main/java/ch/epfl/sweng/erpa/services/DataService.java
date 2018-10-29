package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import ch.epfl.sweng.erpa.model.UuidObject;

public interface DataService<T extends UuidObject> {
    Optional<T> getOne(String uuid);
    Stream<T> getAll();
    void saveOne(T t);
    void removeAll();
}
