package ca.otams.group36;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.otams.group36.activities.StudentRegistrationActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.google.android.material.textfield.TextInputLayout;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import androidx.test.espresso.matcher.BoundedMatcher;

@RunWith(AndroidJUnit4.class)
public class InvalidNameUITest {

    /**
     * Custom matcher to check TextInputLayout error text.
     */
    public static Matcher<Object> hasTextInputLayoutErrorText(final String expectedErrorText) {
        return new BoundedMatcher<Object, TextInputLayout>(TextInputLayout.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("TextInputLayout with error text: " + expectedErrorText);
            }

            @Override
            protected boolean matchesSafely(TextInputLayout textInputLayout) {
                CharSequence error = textInputLayout.getError();
                if (error == null) {
                    return false;
                }
                String errorString = error.toString();
                return expectedErrorText.equals(errorString);
            }
        };
    }

    @Rule
    public ActivityScenarioRule<StudentRegistrationActivity> activityRule =
            new ActivityScenarioRule<>(StudentRegistrationActivity.class);

    @Test
    public void invalidFirstName_ShowsErrorText() {
        // type invalid input
        onView(withId(R.id.etFirstName))
                .perform(typeText("1"), closeSoftKeyboard());

        onView(withId(R.id.btnRegister)).perform(click());

        // check error text in TextInputLayout
        onView(withId(R.id.tilFirstName))
                .check(matches(hasDescendant(withText("Name is invalid"))));
    }

    @Test
    public void invalidLastName_ShowsErrorText() {
        onView(withId(R.id.etLastName))
                .perform(typeText("2"), closeSoftKeyboard());

        onView(withId(R.id.btnRegister)).perform(click());

        onView(withId(R.id.tilLastName))
                .check(matches(hasTextInputLayoutErrorText("Last name is invalid")));
    }
}