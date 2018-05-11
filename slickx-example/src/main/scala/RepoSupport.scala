import com.github.jccode.slickx.core.AbstractRepo
import dao.Tables
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

/**
  * RepoSupport
  *
  * @author 01372461
  */
trait RepoSupport extends Tables {

  val config = DatabaseConfig.forConfig[JdbcProfile]("mydb")
  override val profile = slick.jdbc.H2Profile

  class UserRepo extends AbstractRepo[JdbcProfile, User, UserTable, TableQuery[UserTable]](config, users)
  class AccountRepo extends AbstractRepo[JdbcProfile, Account, AccountTable, TableQuery[AccountTable]](config, accounts)
  class ProductRepo extends AbstractRepo[JdbcProfile, Product, ProductTable, TableQuery[ProductTable]](config, products)

  val userRepo = new UserRepo
  val accountRepo = new AccountRepo
  val productRepo = new ProductRepo
}

object RepoSupport extends RepoSupport
