package com.rxlogix.util

class Tuple4<T1, T2, T3, T4> {
    T1 v1
    T2 v2
    T3 v3
    T4 v4

    public Tuple4(T1 pv1, T2 pv2, T3 pv3, T4 pv4) {
        v1 = pv1
        v2 = pv2
        v3 = pv3
        v4 = pv4
    }

    T1 getValue1() { v1 }
    T2 getValue2() { v2 }
    T3 getValue3() { v3 }
    T4 getValue4() { v4 }

    String toJson() {
        "{\"value1\": \"${v1.toString()}\", \"value2\": \"${v2.toString()}\", " +
                "\"value3\": \"${v3.toString()}\",  \"value4\": \"${v4.toString()}\"}"
    }
}
