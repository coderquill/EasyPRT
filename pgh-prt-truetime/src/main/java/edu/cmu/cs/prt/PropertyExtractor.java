package edu.cmu.cs.prt;

/**
 * Functional interface for extracting a property of a specified type from a HistoryEntry.
 * This interface is designed to be used with lambda expressions or method references
 * to dynamically specify how a particular property should be retrieved from a HistoryEntry instance.
 *
 * <p>The PropertyExtractor interface is a key component in creating flexible and reusable
 * filter methods within the HistoryTable class. It allows for the definition of generic filters
 * that can operate on any property of a HistoryEntry, as long as the property's type matches
 * the generic type parameter of the PropertyExtractor.</p>
 *
 * <p>Usage of this interface makes the filter implementation in HistoryTable more robust and
 * adaptable, as it can easily accommodate new properties or changes in the HistoryEntry structure
 * without requiring changes to the filtering logic itself.</p>
 *
 * <p>Example Usage:</p>
 * <pre>{@code
 * // Creating a PropertyExtractor for the route ID of a HistoryEntry
 * PropertyExtractor<String> routeIdExtractor =  historyEntry -> historyEntry.route().routeId();
 *
 * // Using the extractor in a filter method
 * HistoryTable filteredTable = historyTable.filterBy(routeIdExtractor, "71A");
 * }</pre>
 *
 * @param <T> The type of the property to be extracted. This type parameter ensures that
 *           the extractor is type-safe and can only be used with properties of a consistent type.
 */
@FunctionalInterface
interface PropertyExtractor<T> {
    /**
     * Extracts a property from a HistoryEntry.
     * The specific property and how it is extracted is defined by the lambda expression or method reference
     * implementing this interface.
     *
     * @param entry The HistoryEntry from which to extract the property.
     * @return The extracted property of type T.
     */
    T extract(HistoryEntry entry);
}
