package at.jku.dke.etutor.sparql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Record which represents the full text parameters.
 *
 * @param queryText      the required query search text
 * @param querySubject   the required query subject
 * @param queryPredicate the optional search predicate (depending on the SPARQL store implementation)
 */
public record FulltextParameters(@NotNull String queryText, @NotNull String querySubject,
                                 @Nullable String queryPredicate) {
}
