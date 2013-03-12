/*
 * This file is part of BBCT for Android.
 *
 * Copyright 2012 codeguru <codeguru@users.sourceforge.net>
 *
 * BBCT for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BBCT for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bbct.android.common;

import android.app.Activity;
import android.app.Instrumentation;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import bbct.common.data.BaseballCard;
import java.util.List;
import junit.framework.Assert;

/**
 * Utility methods used for JUnit tests on classes in Android version of BBCT.
 *
 * @author codeguru <codeguru@users.sourceforge.net>
 */
abstract public class BBCTTestUtil {

    /**
     * Assert that the given ListView contains same data as the given list of
     * {@link bbct.common.data.BaseballCard}s.
     *
     * @param inst The instrumentation for the running test case. Used to
     * synchronize this assertion with the instrumented activity class being
     * tested.
     * @param expectedItems A List of the expected
     * {@link bbct.common.data.BaseballCard} data.
     * @param listView The List view to check for
     * {@link bbct.common.data.BaseballCard} data.
     */
    public static void assertListViewContainsItems(Instrumentation inst, List<BaseballCard> expectedItems, ListView listView) {
        inst.waitForIdleSync();
        Assert.assertEquals(expectedItems.size(), listView.getCount());

        for (int i = 0; i < expectedItems.size(); ++i) {
            View row = listView.getChildAt(i);
            TextView brandTextView = (TextView) row.findViewById(R.id.brand_text_view);
            TextView yearTextView = (TextView) row.findViewById(R.id.year_text_view);
            TextView numberTextView = (TextView) row.findViewById(R.id.number_text_view);
            TextView playerNameTextView = (TextView) row.findViewById(R.id.player_name_text_view);

            BaseballCard expectedCard = expectedItems.get(i);
            Assert.assertEquals(expectedCard.getBrand(), brandTextView.getText().toString());
            Assert.assertEquals(expectedCard.getYear(), Integer.parseInt(yearTextView.getText().toString()));
            Assert.assertEquals(expectedCard.getNumber(), Integer.parseInt(numberTextView.getText().toString()));
            Assert.assertEquals(expectedCard.getPlayerName(), playerNameTextView.getText().toString());
        }
    }

    /**
     * Test that a menu item correctly launches a child activity.
     *
     * @param inst The instrumentation used for this test.
     * @param activity The activity associated with the menu being tested.
     * @param menuId The id of the menu resource.
     * @param childActivityClass The Class of the child activity which should be
     * launched.
     * @return A reference to the child activity which is launched, if the test
     * succeeds.
     */
    public static Activity testMenuItem(Instrumentation inst, Activity activity, int menuId, Class childActivityClass) {
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(childActivityClass.getName(), null, false);
        inst.addMonitor(monitor);

        Assert.assertTrue(inst.invokeMenuActionSync(activity, menuId, MENU_FLAGS));

        Activity childActivity = inst.waitForMonitorWithTimeout(monitor, TIME_OUT);

        Assert.assertNotNull(childActivity);
        Assert.assertEquals(childActivityClass, childActivity.getClass());

        return childActivity;
    }

    /**
     * Add a card to the database using the TextViews from the given
     * @link bbct.android.common BaseballCardDetails} activity and check that
     * the save button can be clicked.
     *
     * @param test The InstrumentationTestCase object performing the test.
     * @param cardDetails The {@link bbct.android.common.BaseballCardDetails}
     * activity being tested.
     * @param card The {@link bbct.common.data.BaseballCard} object holding the
     * baseball card data to add to the database.
     */
    public static void addCard(InstrumentationTestCase test, Activity cardDetails, BaseballCard card) {
        BBCTTestUtil.sendKeysToCardDetails(test, cardDetails, card);

        final Button saveButton = (Button) cardDetails.findViewById(R.id.save_button);

        test.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(saveButton.performClick());

                // TODO Check that Toast appears with correct message.
            }
        });
    }

    /**
     * Click the "Done" button on the given
     * {@link bbct.android.common.BaseballCardDetails} activity and assert that
     * the activity is finishing. This is all wrapped into a helper method
     * because the button click must be done on the UI thread while the
     * assertion must not.
     *
     * @param inst The Instrumentation instance that controls the UI thread.
     * @param cardDetails The {@link bbctandroid.common.BaseballCardDetails}
     * activity being tested.
     */
    public static void clickCardDetailsDone(Instrumentation inst, Activity cardDetails) {
        final Button doneButton = (Button) cardDetails.findViewById(R.id.done_button);

        inst.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Assert.assertTrue(doneButton.performClick());
            }
        });

        Assert.assertTrue(cardDetails.isFinishing());
    }

    /**
     * Fills in all EditText views of the given
     * {@link bbct.android.common.BaseballCardDetails} activity.
     *
     * @param test The InstrumentationTestCase object performing the test.
     * @param cardDetails The {@link bbct.android.common.BaseballCardDetails}
     * activity being tested.
     * @param card The {@link bbct.common.data.BaseballCard} object holding the
     * baseball card data to add to the database.
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static void sendKeysToCardDetails(InstrumentationTestCase test, Activity cardDetails, BaseballCard card) {
        BBCTTestUtil.sendKeysToCardDetails(test, cardDetails, card, ALL_FIELDS);
    }

    /**
     * Fills in all EditText views, except the ones indicated, of the given
     * {@link bbct.android.common.BaseballCardDetails} activity.
     *
     * @param test The InstrumentationTestCase object performing the test.
     * @param cardDetails The {@link bbct.android.common.BaseballCardDetails}
     * activity being tested.
     * @param card The {@link bbct.common.data.BaseballCard} object holding the
     * baseball card data to add to the database.
     * @param skipFlags The EditText views to skip filling in.
     */
    public static void sendKeysToCardDetails(InstrumentationTestCase test, Activity cardDetails, BaseballCard card, int skipFlags) {
        Log.d(TAG, "sendKeysToCardDetails()");

        Instrumentation inst = test.getInstrumentation();

        if ((skipFlags & BRAND_FIELD) > 0) {
            inst.sendStringSync(card.getBrand());
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ESCAPE);
        }
        AutoCompleteTextView brandText = (AutoCompleteTextView) cardDetails.findViewById(R.id.brand_text);
        if (brandText.isPopupShowing()) {
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        if ((skipFlags & YEAR_FIELD) > 0) {
            inst.sendStringSync(Integer.toString(card.getYear()));
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        if ((skipFlags & NUMBER_FIELD) > 0) {
            inst.sendStringSync(Integer.toString(card.getNumber()));
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        if ((skipFlags & VALUE_FIELD) > 0) {
            String valueStr = String.format("%.2f", card.getValue() / 100.0);
            inst.sendStringSync(valueStr);
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        if ((skipFlags & COUNT_FIELD) > 0) {
            inst.sendStringSync(Integer.toString(card.getCount()));
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        if ((skipFlags & PLAYER_NAME_FIELD) > 0) {
            inst.sendStringSync(card.getPlayerName());
        }
        AutoCompleteTextView playerNameText = (AutoCompleteTextView) cardDetails.findViewById(R.id.player_name_text);
        if (playerNameText.isPopupShowing()) {
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);

        Spinner playerPositionSpinner = (Spinner) cardDetails.findViewById(R.id.player_position_text);
        ArrayAdapter<CharSequence> playerPositionAdapter = (ArrayAdapter<CharSequence>) playerPositionSpinner.getAdapter();
        int newPos = playerPositionAdapter.getPosition(card.getPlayerPosition());
        int oldPos = playerPositionSpinner.getSelectedItemPosition();
        int move = newPos - oldPos;

        Log.d(TAG, "newPos=" + newPos + ", oldPos=" + oldPos + ", move=" + move);

        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        if (move > 0) {
            test.sendRepeatedKeys(move, KeyEvent.KEYCODE_DPAD_DOWN);
        } else {
            test.sendRepeatedKeys(-move, KeyEvent.KEYCODE_DPAD_UP);
        }
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
    }

    private BBCTTestUtil() {
    }
    /**
     * Leave all fields blank.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int NO_FIELDS = 0x00;
    /**
     * Include the card brand field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int BRAND_FIELD = 0x01;
    /**
     * Include the card year field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int YEAR_FIELD = 0x02;
    /**
     * Include the card number field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int NUMBER_FIELD = 0x04;
    /**
     * Include the card value field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int VALUE_FIELD = 0x08;
    /**
     * Include the card count field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int COUNT_FIELD = 0x10;
    /**
     * Include the player name field.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int PLAYER_NAME_FIELD = 0x20;
    /**
     * Include all input fields.
     *
     * @see sendKeysToCardDetails(InstrumentationTestCase, Activity,
     * BaseballCard, int)
     */
    public static final int ALL_FIELDS = BRAND_FIELD | YEAR_FIELD | NUMBER_FIELD | VALUE_FIELD | COUNT_FIELD | PLAYER_NAME_FIELD;
    /**
     * Asset file which contains card data as CSV values.
     */
    public static final String CARD_DATA = "cards.csv";
    private static final int MENU_FLAGS = 0;
    private static final int TIME_OUT = 5 * 1000; // 5 seconds
    private static final String TAG = BBCTTestUtil.class.getName();
}
