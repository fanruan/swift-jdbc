package com.fr.swift.source.manager;

import com.fr.swift.increment.Increment;
import com.fr.swift.source.DataSource;
import com.fr.swift.source.IRelationSource;
import com.fr.swift.source.SourcePath;

import java.util.List;

/**
 * This class created on 2017-11-27.
 *
 * @author Lucifer
 * @since Advanced FineBI Analysis 1.0
 */
public interface IndexStuffProvider {

    DataSource getTableById(String sourceId);

    List<DataSource> getTablesByIds(List<String> sourceIds);

    IRelationSource getRelationById(String sourceId);

    List<IRelationSource> getRelationsByIds(List<String> sourceIds);

    SourcePath getPathById(String sourceId);

    List<SourcePath> getPathsByIds(List<String> sourceIds);

    List<DataSource> getAllTables();

    List<IRelationSource> getAllRelations();

    List<SourcePath> getAllPaths();

    List<Increment> getIncrementBySourceId(String sourceId);
}
