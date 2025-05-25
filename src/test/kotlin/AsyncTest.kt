import exceptions.InvalidTypeException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class AsyncTest {

    @Test
    fun `test section async get operations`() = runBlocking {
        val section = Section("TestSection")
        section.addKey("StringKey", "StringValue")
        section.addKey("IntKey", 42)
        section.addKey("FloatKey", 3.14f)
        section.addKey("BoolKey", true)
        section.addKey("StructKey", mapOf("Key1" to "Value1", "Key2" to 42))
        section.addArrayKey("ArrayKey", listOf("value1", "value2", "value3"))
        section.addIndexedArrayKey("IndexedArrayKey", mapOf(0 to "value0", 2 to "value2"))
        section.addMapKey("MapKey", mapOf("SubKey1" to "Value1", "SubKey2" to "Value2"))

        // Test async getters
        val value = section.getKeyAsync("StringKey")
        assertTrue(value is StringValue)
        assertEquals("StringValue", value.toString())
        assertEquals("StringValue", section.getStringKeyAsync("StringKey"))
        assertEquals(42, section.getIntKeyAsync("IntKey"))
        assertEquals(3.14f, section.getFloatKeyAsync("FloatKey"))
        assertEquals(true, section.getBooleanKeyAsync("BoolKey"))

        val structValue = section.getStructKeyAsync("StructKey")
        assertEquals("Value1", structValue["Key1"])
        assertEquals(42, structValue["Key2"])

        val arrayValue = section.getArrayKeyAsync("ArrayKey")
        assertEquals(3, arrayValue.size)
        assertEquals(listOf("value1", "value2", "value3"), arrayValue)

        val indexedArrayValue = section.getIndexedArrayKeyAsync("IndexedArrayKey")
        assertEquals("value0", indexedArrayValue[0])
        assertEquals("value2", indexedArrayValue[2])

        val mapValue = section.getMapKeyAsync("MapKey")
        assertEquals("Value1", mapValue["SubKey1"])
        assertEquals("Value2", mapValue["SubKey2"])

        // Test non-existent key
        assertThrows<NoSuchElementException> { section.getKeyAsync("NonExistentKey") }

        // Test type mismatch
        assertThrows<InvalidTypeException> { section.getIntKeyAsync("StringKey") }
    }

    @Test
    fun `test section async set operations`() = runBlocking {
        val section = Section("TestSection")

        // Test setting values asynchronously
        section.setKeyAsync("StringKey", "StringValue")
        section.setKeyAsync("IntKey", 42)
        section.setKeyAsync("FloatKey", 3.14f)
        section.setKeyAsync("BoolKey", true)
        section.setKeyAsync("StructKey", mapOf("Key1" to "Value1", "Key2" to 42))
        section.setArrayKeyAsync("ArrayKey", listOf("value1", "value2", "value3"))
        section.setIndexedArrayKeyAsync("IndexedArrayKey", mapOf(0 to "value0", 2 to "value2"))
        section.setMapKeyAsync("MapKey", mapOf("SubKey1" to "Value1", "SubKey2" to "Value2"))

        // Verify values were set correctly
        assertEquals("StringValue", section.getStringKeyAsync("StringKey"))
        assertEquals(42, section.getIntKeyAsync("IntKey"))
        assertEquals(3.14f, section.getFloatKeyAsync("FloatKey"))
        assertEquals(true, section.getBooleanKeyAsync("BoolKey"))

        val structValue = section.getStructKeyAsync("StructKey")
        assertEquals("Value1", structValue["Key1"])
        assertEquals(42, structValue["Key2"])

        val arrayValue = section.getArrayKeyAsync("ArrayKey")
        assertEquals(3, arrayValue.size)
        assertEquals(listOf("value1", "value2", "value3"), arrayValue)

        val indexedArrayValue = section.getIndexedArrayKeyAsync("IndexedArrayKey")
        assertEquals("value0", indexedArrayValue[0])
        assertEquals("value2", indexedArrayValue[2])

        val mapValue = section.getMapKeyAsync("MapKey")
        assertEquals("Value1", mapValue["SubKey1"])
        assertEquals("Value2", mapValue["SubKey2"])

        // Test updating values asynchronously
        section.setKeyAsync("StringKey", "UpdatedValue")
        section.setKeyAsync("IntKey", 100)
        section.setKeyAsync("FloatKey", 2.71f)
        section.setKeyAsync("BoolKey", false)

        // Verify updates
        assertEquals("UpdatedValue", section.getStringKeyAsync("StringKey"))
        assertEquals(100, section.getIntKeyAsync("IntKey"))
        assertEquals(2.71f, section.getFloatKeyAsync("FloatKey"))
        assertEquals(false, section.getBooleanKeyAsync("BoolKey"))
    }

    @Test
    fun `test section async add operations`() = runBlocking {
        val section = Section("TestSection")

        // Test adding values asynchronously
        section.addKeyAsync("StringKey", "StringValue")
        section.addKeyAsync("IntKey", 42)
        section.addKeyAsync("FloatKey", 3.14f)
        section.addKeyAsync("BoolKey", true)
        section.addKeyAsync("StructKey", mapOf("Key1" to "Value1", "Key2" to 42))
        section.addArrayKeyAsync("ArrayKey", listOf("value1", "value2", "value3"))
        section.addIndexedArrayKeyAsync("IndexedArrayKey", mapOf(0 to "value0", 2 to "value2"))
        section.addMapKeyAsync("MapKey", mapOf("SubKey1" to "Value1", "SubKey2" to "Value2"))

        // Verify values were added correctly
        assertEquals("StringValue", section.getStringKeyAsync("StringKey"))
        assertEquals(42, section.getIntKeyAsync("IntKey"))
        assertEquals(3.14f, section.getFloatKeyAsync("FloatKey"))
        assertEquals(true, section.getBooleanKeyAsync("BoolKey"))

        val structValue = section.getStructKeyAsync("StructKey")
        assertEquals("Value1", structValue["Key1"])
        assertEquals(42, structValue["Key2"])

        val arrayValue = section.getArrayKeyAsync("ArrayKey")
        assertEquals(3, arrayValue.size)
        assertEquals(listOf("value1", "value2", "value3"), arrayValue)

        val indexedArrayValue = section.getIndexedArrayKeyAsync("IndexedArrayKey")
        assertEquals("value0", indexedArrayValue[0])
        assertEquals("value2", indexedArrayValue[2])

        val mapValue = section.getMapKeyAsync("MapKey")
        assertEquals("Value1", mapValue["SubKey1"])
        assertEquals("Value2", mapValue["SubKey2"])

        // Test adding duplicate keys throws exception
        assertThrows<IllegalArgumentException> { section.addKeyAsync("StringKey", "DuplicateValue") }
        assertThrows<IllegalArgumentException> { section.addArrayKeyAsync("ArrayKey", listOf("duplicate")) }
        assertThrows<IllegalArgumentException> { section.addIndexedArrayKeyAsync("IndexedArrayKey", mapOf(3 to "duplicate")) }
        assertThrows<IllegalArgumentException> { section.addMapKeyAsync("MapKey", mapOf("SubKey3" to "duplicate")) }
    }

    @Test
    fun `test section async delete operation`() = runBlocking {
        val section = Section("TestSection")
        section.addKey("Key1", "Value1")
        section.addKey("Key2", "Value2")

        // Delete a key asynchronously
        section.deleteKeyAsync("Key1")

        // Verify key was deleted
        assertThrows<NoSuchElementException> { section.getKeyAsync("Key1") }
        assertEquals("Value2", section.getStringKeyAsync("Key2"))

        // Test deleting non-existent key
        assertThrows<NoSuchElementException> { section.deleteKeyAsync("NonExistentKey") }
    }

    @Test
    fun `test iniFile async section operations`() = runBlocking {
        val iniFile = IniFile(emptyList())

        // Test adding sections asynchronously
        val section1 = iniFile.addSectionAsync("Section1")
        val section2 = iniFile.addSectionAsync("Section2")

        // Get sections asynchronously
        val retrievedSection1 = iniFile.getSectionAsync("Section1")
        val retrievedSection2 = iniFile.getSectionAsync("Section2")

        // Verify sections were added and retrieved correctly
        assertEquals("Section1", retrievedSection1.name)
        assertEquals("Section2", retrievedSection2.name)

        // Test section exists check
        assertTrue(iniFile.hasSectionAsync("Section1"))
        assertTrue(iniFile.hasSectionAsync("Section2"))
        assertFalse(iniFile.hasSectionAsync("NonExistentSection"))

        // Delete a section asynchronously
        iniFile.deleteSectionAsync("Section1")

        // Verify section was deleted
        assertFalse(iniFile.hasSectionAsync("Section1"))
        assertTrue(iniFile.hasSectionAsync("Section2"))

        // Test deleting non-existent section
        assertThrows<NoSuchElementException> { iniFile.deleteSectionAsync("NonExistentSection") }
    }

    @Test
    fun `test iniFile async value operations`() = runBlocking {
        val iniFile = IniFile(emptyList())
        iniFile.addSectionAsync("Section1")

        // Test setting values asynchronously
        iniFile.setValueAsync("Section1", "StringKey", "StringValue")
        iniFile.setValueAsync("Section1", "IntKey", 42)
        iniFile.setValueAsync("Section1", "FloatKey", 3.14f)
        iniFile.setValueAsync("Section1", "BoolKey", true)
        iniFile.setValueAsync("Section1", "StructKey", mapOf("Key1" to "Value1", "Key2" to 42))
        iniFile.setArrayValueAsync("Section1", "ArrayKey", listOf("value1", "value2", "value3"))
        iniFile.setIndexedArrayValueAsync("Section1", "IndexedArrayKey", mapOf(0 to "value0", 2 to "value2"))
        iniFile.setMapValueAsync("Section1", "MapKey", mapOf("SubKey1" to "Value1", "SubKey2" to "Value2"))

        // Test getting values asynchronously
        assertEquals("StringValue", iniFile.getStringValueAsync("Section1", "StringKey"))
        assertEquals(42, iniFile.getIntValueAsync("Section1", "IntKey"))
        assertEquals(3.14f, iniFile.getFloatValueAsync("Section1", "FloatKey"))
        assertEquals(true, iniFile.getBooleanValueAsync("Section1", "BoolKey"))

        val structValue = iniFile.getStructValueAsync("Section1", "StructKey")
        assertEquals("Value1", structValue["Key1"])
        assertEquals(42, structValue["Key2"])

        val arrayValue = iniFile.getArrayValueAsync("Section1", "ArrayKey")
        assertEquals(3, arrayValue.size)
        assertEquals(listOf("value1", "value2", "value3"), arrayValue)

        val indexedArrayValue = iniFile.getIndexedArrayValueAsync("Section1", "IndexedArrayKey")
        assertEquals("value0", indexedArrayValue[0])
        assertEquals("value2", indexedArrayValue[2])

        val mapValue = iniFile.getMapValueAsync("Section1", "MapKey")
        assertEquals("Value1", mapValue["SubKey1"])
        assertEquals("Value2", mapValue["SubKey2"])

        // Test adding values asynchronously
        iniFile.addValueAsync("Section1", "NewStringKey", "NewValue")
        iniFile.addValueAsync("Section1", "NewIntKey", 100)
        iniFile.addValueAsync("Section1", "NewFloatKey", 1.23f)
        iniFile.addValueAsync("Section1", "NewBoolKey", false)
        iniFile.addValueAsync("Section1", "NewStructKey", mapOf("NewKey1" to "NewValue1"))
        iniFile.addArrayValueAsync("Section1", "NewArrayKey", listOf("new1", "new2"))
        iniFile.addIndexedArrayValueAsync("Section1", "NewIndexedArrayKey", mapOf(5 to "value5"))
        iniFile.addMapValueAsync("Section1", "NewMapKey", mapOf("NewSubKey" to "NewSubValue"))

        // Verify added values
        assertEquals("NewValue", iniFile.getStringValueAsync("Section1", "NewStringKey"))
        assertEquals(100, iniFile.getIntValueAsync("Section1", "NewIntKey"))
        assertEquals(1.23f, iniFile.getFloatValueAsync("Section1", "NewFloatKey"))
        assertEquals(false, iniFile.getBooleanValueAsync("Section1", "NewBoolKey"))
        assertEquals("NewValue1", iniFile.getStructValueAsync("Section1", "NewStructKey")["NewKey1"])
        assertEquals(listOf("new1", "new2"), iniFile.getArrayValueAsync("Section1", "NewArrayKey"))
        assertEquals("value5", iniFile.getIndexedArrayValueAsync("Section1", "NewIndexedArrayKey")[5])
        assertEquals("NewSubValue", iniFile.getMapValueAsync("Section1", "NewMapKey")["NewSubKey"])

        // Test deleting a value asynchronously
        iniFile.deleteValueAsync("Section1", "StringKey")

        // Verify value was deleted
        assertThrows<NoSuchElementException> { iniFile.getValueAsync("Section1", "StringKey") }
    }

    @Test
    fun `test concurrent access to section`() = runBlocking {
        val section = Section("ConcurrentSection")
        val iterations = 100
        val threadCount = 10

        // Set up a latch to synchronize thread start
        val startLatch = CountDownLatch(1)
        val completionLatch = CountDownLatch(threadCount)
        val errors = ConcurrentHashMap<Int, Throwable>()

        // Launch multiple threads to concurrently modify the section
        for (threadId in 0 until threadCount) {
            thread {
                try {
                    startLatch.await()

                    for (i in 0 until iterations) {
                        val key = "key-${threadId}-${i}"
                        val value = "value-${threadId}-${i}"

                        // Perform mixed operations
                        section.addKey(key, value)
                        section.getKey(key)
                        section.setKey(key, "${value}-updated")
                        section.deleteKey(key)
                    }
                } catch (e: Throwable) {
                    errors[threadId] = e
                } finally {
                    completionLatch.countDown()
                }
            }
        }

        // Start all threads at once
        startLatch.countDown()

        // Wait for all threads to complete
        completionLatch.await(30, TimeUnit.SECONDS)

        // Check for errors
        assertTrue(errors.isEmpty(), "Errors occurred during concurrent access: $errors")
    }

    @Test
    fun `test concurrent access using async functions`() = runBlocking {
        val section = Section("AsyncConcurrentSection")
        val iterations = 100
        val coroutineCount = 10
        val errors = mutableListOf<Throwable>()

        coroutineScope {
            val jobs = List(coroutineCount) { coroutineId ->
                launch {
                    try {
                        for (i in 0 until iterations) {
                            val key = "key-${coroutineId}-${i}"
                            val value = "value-${coroutineId}-${i}"

                            // Perform mixed async operations
                            section.addKeyAsync(key, value)
                            section.getKeyAsync(key)
                            section.setKeyAsync(key, "${value}-updated")
                            section.deleteKeyAsync(key)
                        }
                    } catch (e: Throwable) {
                        synchronized(errors) {
                            errors.add(e)
                        }
                    }
                }
            }

            // Wait for all coroutines to complete
            jobs.forEach { it.join() }
        }

        // Check for errors
        assertTrue(errors.isEmpty(), "Errors occurred during concurrent async access: $errors")
    }

    @Test
    fun `test iniFile concurrent section operations`() = runBlocking {
        val iniFile = IniFile(emptyList())
        val iterationsPerThread = 10
        val threadCount = 5
        val successCount = AtomicInteger(0)

        coroutineScope {
            val jobs = List(threadCount) { threadId ->
                launch {
                    for (i in 0 until iterationsPerThread) {
                        val sectionName = "Section-${threadId}-${i}"
                        val section = iniFile.addSectionAsync(sectionName)

                        // Add some entries
                        section.addKeyAsync("StringKey", "Value-${threadId}-${i}")
                        section.addKeyAsync("IntKey", i)

                        // Verify section exists
                        if (iniFile.hasSectionAsync(sectionName)) {
                            // Get and verify the section
                            val retrievedSection = iniFile.getSectionAsync(sectionName)
                            if (retrievedSection.name == sectionName) {
                                // Delete it
                                iniFile.deleteSectionAsync(sectionName)
                                successCount.incrementAndGet()
                            }
                        }
                    }
                }
            }

            jobs.forEach { it.join() }
        }

        // Verify expected number of operations succeeded
        assertEquals(threadCount * iterationsPerThread, successCount.get())
    }

    @Test
    fun `test interleaved blocking and async operations`() = runBlocking {
        val section = Section("MixedAccessSection")

        // Setup completed flags
        val asyncOpCompleted = AtomicBoolean(false)
        val blockingOpCompleted = AtomicBoolean(false)

        // Start a coroutine that will perform async operations
        val asyncJob = launch {
            // Add some keys asynchronously
            section.addKeyAsync("AsyncKey1", "AsyncValue1")
            section.addKeyAsync("AsyncKey2", "AsyncValue2")
            section.addKeyAsync("SharedKey", "AsyncValue")

            // Signal completion of the initial async operations
            asyncOpCompleted.set(true)

            // Wait until blocking operations are done
            while (!blockingOpCompleted.get()) {
                delay(10)
            }

            // Update the shared key
            section.setKeyAsync("SharedKey", "FinalAsyncValue")

            // Add one more key
            section.addKeyAsync("AsyncKey3", "AsyncValue3")
        }

        // Perform blocking operations in the main thread

        // Wait until async operations have started
        while (!asyncOpCompleted.get()) {
            delay(10)
        }

        // Now perform some blocking operations
        section.addKey("BlockingKey1", "BlockingValue1")
        section.addKey("BlockingKey2", "BlockingValue2")

        // Try to update the shared key
        section.setKey("SharedKey", "BlockingValue")

        // Signal completion of blocking operations
        blockingOpCompleted.set(true)

        // Wait for async operations to finish
        asyncJob.join()

        // Verify the final state
        assertEquals("BlockingValue1", section.getStringKey("BlockingKey1"))
        assertEquals("BlockingValue2", section.getStringKey("BlockingKey2"))
        assertEquals("AsyncValue1", section.getStringKey("AsyncKey1"))
        assertEquals("AsyncValue2", section.getStringKey("AsyncKey2"))
        assertEquals("AsyncValue3", section.getStringKey("AsyncKey3"))

        // The last writer should win for SharedKey
        assertEquals("FinalAsyncValue", section.getStringKey("SharedKey"))
    }
}
