package com.moorango.medli;


import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.PerformanceTestCase;

import com.moorango.medli.Helpers.DataCheck;
import com.moorango.medli.Helpers.DateTime;
import com.moorango.medli.Models.MedLog;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@SuppressWarnings("ALL")
public class Activity_MedLi_lightTest extends AndroidTestCase implements PerformanceTestCase {

    public Activity_MedLi_lightTest() {
        super();
    }

    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    private static final int CURRENT_DATABASE_VERSION = 1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
    }

    public boolean isPerformanceOnly() {
        return false;
    }

    // These test can only be run once.
    public int startPerformance(Intermediates intermediates) {
        return 1;
    }

    /**
     * com.moorango.medli.helpers.DateTime function tests.
     */

    public void testSomething() {
        String startTimeStamp = "2014-09-17 12:00:00";
        String incrementedHour = "2014-09-17 16:00:00";
        String incrementedDay = "2014-09-18 12:00:00";
        String incrementedMinute = "2014-09-17 12:32:00";

        assertEquals("increment hour test", incrementedHour, DateTime.getIncrementedTimestamp(startTimeStamp, 0, 4, 0));
        assertEquals("increment day test", incrementedDay, DateTime.getIncrementedTimestamp(startTimeStamp, 1, 0, 0));
        assertEquals("increment minute test", incrementedMinute, DateTime.getIncrementedTimestamp(startTimeStamp, 0, 0, 32));

    }

    public void testDoseTimeFrame() {

        String currentDateTime = DateTime.getCurrentTimestamp(true, "1:37 PM");

        String earlyDateTime = DateTime.getCurrentTimestamp(true, "1:59 PM");
        String onTimeDateTime = DateTime.getCurrentTimestamp(true, "1:30 PM");
        String lateDateTime = DateTime.getCurrentTimestamp(true, "1:10 PM");

        String invalidParam = "2014-8-17 01:45:00";

        assertEquals("earlyDateTime", MedLog.EARLY, DataCheck.getDoseTimeFrame(currentDateTime, earlyDateTime));
        assertEquals("onTimeDateTime", MedLog.ON_TIME, DataCheck.getDoseTimeFrame(currentDateTime, onTimeDateTime));
        assertEquals("lateDateTime", MedLog.LATE, DataCheck.getDoseTimeFrame(currentDateTime, lateDateTime));
        assertEquals("invalidParam", 400, DataCheck.getDoseTimeFrame(currentDateTime, invalidParam));

    }

    public void testUniqueIdCreator() {

        Set<Integer> lump = new HashSet<Integer>();


        for (int index = 0; index < 100; index++) {

            int id = DataCheck.createUniqueID("f");
            assertTrue(!lump.contains(id));
            lump.add(DataCheck.createUniqueID("f"));
        }

    }

    public void testNowInMilliseconds() {
        // Time t = new Time();
        // t.setToNow();

    }
}