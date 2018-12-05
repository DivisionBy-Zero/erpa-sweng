package ch.epfl.sweng.erpa.util;

import lombok.Data;
import lombok.NonNull;

@Data
public class Triplet<T1, T2, T3> {
    @NonNull T1 first;
    @NonNull T2 second;
    @NonNull T3 third;
}
