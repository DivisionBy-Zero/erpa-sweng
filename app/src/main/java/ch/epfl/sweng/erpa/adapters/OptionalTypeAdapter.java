package ch.epfl.sweng.erpa.adapters;

import android.support.annotation.NonNull;

import com.annimon.stream.Optional;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {
    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override public <U> TypeAdapter<U> create(Gson gson, TypeToken<U> type) {
            Class<U> raw = (Class<U>) type.getRawType();
            if (raw != Optional.class) {
                return null;
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                Type innerType = parameterizedType.getActualTypeArguments()[0];
                TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(innerType));
                return new OptionalTypeAdapter(adapter);
            }
        }
    };

    @NonNull private TypeAdapter<T> innerAdapter;

    @Override public void write(JsonWriter out, Optional<T> value) throws IOException {
        if (value.isPresent()) {
            innerAdapter.write(out, value.get());
        } else {
            out.nullValue();
        }
    }

    @Override public Optional<T> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.skipValue();
            return Optional.empty();
        } else {
            return Optional.ofNullable(innerAdapter.read(in));
        }
    }
}
