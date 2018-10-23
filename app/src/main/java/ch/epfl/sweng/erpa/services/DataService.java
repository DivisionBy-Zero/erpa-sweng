package ch.epfl.sweng.erpa.services;

import com.annimon.stream.Optional;

import java.util.Set;

import ch.epfl.sweng.erpa.model.UuidObject;

public interface DataService<T extends UuidObject> {


    Optional<T> getOne(String uid);
    Set<T> getAll();
    void saveOne(T t);
}
