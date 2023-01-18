package com.hoquangnam45.pharmacy.helper;

import java.util.function.Function;

public class Functions {
  public static <T, K> Function<T, K> rethrow(MaybeExceptionFn<T, K> f) {
    return (val) -> {
      try {
        return f.apply(val);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }

  public static interface MaybeExceptionFn<T, K> {
    K apply(T t) throws Exception;
  }
}
