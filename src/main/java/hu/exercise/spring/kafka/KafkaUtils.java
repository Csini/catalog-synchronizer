package hu.exercise.spring.kafka;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class KafkaUtils {
	// https://www.geeksforgeeks.org/convert-an-iterator-to-stream-in-java/
	// Function to get the Stream
	public static <T> Stream<T> getStreamFromIterator(Iterator<T> iterator) {

		// Convert the iterator to Spliterator
		Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, 0);

		// Get a Sequential Stream from spliterator
		return StreamSupport.stream(spliterator, false);
	}
}
