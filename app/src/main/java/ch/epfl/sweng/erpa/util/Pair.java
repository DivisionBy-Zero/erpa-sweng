package ch.epfl.sweng.erpa.util;

import lombok.Data;
import lombok.NonNull;

@Data
public class Pair<T1, T2> {
    @NonNull T1 first;
    @NonNull T2 second;
}

