package ca.otams.group36;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import ca.otams.group36.activities.LoginActivity;

@RunWith(AndroidJUnit4.class)
public class LoginEspressoTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> rule =
            new ActivityScenarioRule<>(LoginActivity.class);

    // 测试：邮箱无效时，应该显示错误信息
    @Test
    public void emailIsInvalid() {
        onView(withId(R.id.editEmail))
                .perform(typeText("abc"), closeSoftKeyboard());   // 明显不是合法邮箱
        onView(withId(R.id.editPassword))
                .perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // 这里的文本要和你 LoginActivity 里实际显示的一样！
        onView(withText("Email is invalid"))
                .check(matches(isDisplayed()));
    }

    // 测试：密码无效时，应该显示错误信息
    @Test
    public void passwordIsInvalid() {
        onView(withId(R.id.editEmail))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword))
                .perform(typeText("123"), closeSoftKeyboard());   // 太短，当作无效密码
        onView(withId(R.id.buttonLogin))
                .perform(click());

        // 同样，这里的字符串要和你实际用的一样
        onView(withText("Password is invalid"))
                .check(matches(withId(R.id.textLoginTitle)));
    }
}
