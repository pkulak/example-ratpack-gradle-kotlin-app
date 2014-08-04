package ratpack.example.kotlin

import ratpack.launch.LaunchConfig
import ratpack.handling.Handler
import ratpack.guice.Guice.handler
import ratpack.handling.ChainAction
import ratpack.handling.Chain
import ratpack.func.Action
import ratpack.guice.BindingsSpec

class HandlerFactory : ratpack.launch.HandlerFactory {
  override fun create(launchConfig : LaunchConfig?) = handler(launchConfig!!, registerModules, routes)

  /**
   * Registers all of the Guice modules that make up the application.
   *
   * This is only invoked once during application bootstrap. If you change the
   * module configuration of an application, you must restart it.
   */
  private val registerModules = Action {(registry : BindingsSpec?) ->
    registry!!.add(MyModule())
  }

  private val routes = Action {(chain : Chain?) ->
    Routes().execute(chain!!)
  }

  private class Routes : ChainAction() {
    /**
     * Adds potential routes.
     *
     * After this method completes, a handler chain will be constructed from
     * the specified routes.
     *
     * This method will be called for every request. This makes it possible
     * to dynamically define the routes if necessary.
     */
    override fun execute() {
      // Map to /foo
      handler("foo") { context -> context.render("from the foo handler") }

      // Map to /bar
      handler("bar") { context -> context.render("from the bar handler") }

      // Set up a nested routing block, which is delegated to `nestedHandler`
      prefix("nested") {(nested : Chain?) ->
        // Map to /nested/*/*
        nested!!.handler(":var1/:var2?") { context ->
          // The path tokens are the :var1 and :var2 path components above
          val pathTokens = context.getPathTokens()!!
          context.render("from the nested handler, var1: ${pathTokens["var1"]}, var2: ${pathTokens["var2"]}")
        }
      }

      // Map to a dependency injected handler
      handler("injected", getRegistry()!![javaClass<MyHandler>()])

      // Bind the /static app path to the src/ratpack/assets/images dir
      // Try /static/logo.png
      prefix("static") {(nested : Chain?) -> nested!!.assets("assets/images") }

      // If nothing above matched, we'll get to here.
      handler { context -> context.render("root handler!") }
    }
  }
}