package tool.xfy9326.schedule.ui.activity.base

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

abstract class AbstractActivity : AppCompatActivity() {
    protected open var useBackInsteadOfNavigateHome: Boolean = true

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityInit(savedInstanceState)
    }

    protected open fun onActivityInit(savedInstanceState: Bundle?) {}

    @CallSuper
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (useBackInsteadOfNavigateHome && item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}