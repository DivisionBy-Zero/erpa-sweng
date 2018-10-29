package ch.epfl.sweng.erpa.services.GCF;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;


import java.util.SortedMap;

import ch.epfl.sweng.erpa.model.UuidObject;
import ch.epfl.sweng.erpa.services.DataService;

abstract class GCFDataService<T extends UuidObject> implements DataService<T> {
    abstract protected SortedMap<String,String> getOneQuery();
    abstract protected SortedMap<String,String> removeQuery();
    abstract protected SortedMap<String,String> saveOneQuery();

    @Override public void saveOne(T t) {
        // TODO (@Sapphie): send request
    }

    @Override public Optional<T> getOne(String uuid) {
        // TODO (@Sapphie): send request
        return Optional.empty();
    }

    @Override public Stream<T> getAll() {
        throw new UnsupportedOperationException("Getting all games from remote server.");
    }

    @Override public void removeAll() {
        // TODO (@Sapphie): send request
    }
}
