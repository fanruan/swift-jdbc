package com.fr.swift.cube.task;

import com.fr.swift.cube.task.impl.Operation;

/**
 * @author anchore
 * @date 2017/12/15
 */
public interface TaskKey {
    String name();

    Operation operation();
}