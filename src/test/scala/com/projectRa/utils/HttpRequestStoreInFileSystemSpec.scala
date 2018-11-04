package com.projectRa.utils

import org.scalatest.Matchers
import org.scalatest.FlatSpec
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.concurrent.ScalaFutures

class HttpRequestStoreInFileSystemSpec extends FlatSpec with Matchers with ScalaFutures {
  
  it should "return a response that was put on it previously" in new CacheInFs with ExampleRquest
                                                                 with ExampleResponse {
    whenReady(for (_ <- cache.putResponse(exampleRequest, exampleResponse);
                   r <- cache.getResponse(exampleRequest)) yield r) { _ shouldBe Some(exampleResponse) }
  }
  
  val tempDir = File.createTempFile("raproject",  "tmp").getParentFile
  
  trait CacheInFs {
    val cache = new HttpRequestStoreInFileSystem {
      val baseDir = tempDir 
    }
  }
  
  trait ExampleRquest {
    import dispatch._
    
    val exampleRequest = url("http://localhost") 
  }
  
  trait ExampleResponse {
    val exampleResponse = "test"
  }
}