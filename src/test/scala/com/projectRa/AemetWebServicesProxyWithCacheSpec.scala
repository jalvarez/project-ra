package com.projectRa

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import utils.HttpRequestCache
import dispatch._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Matchers

class AemetWebServicesProxyWithCacheSpec extends FlatSpec with Matchers with MockFactory with ScalaFutures {
  it should "invoke cache before to get response" in new UnderlyingProxyAndCacheMocks 
                                                      with ProxyWithCache
                                                      with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                    .expects(*, *)
                    .never
    
    (requestCache.getResponse (_:Req)(_: ExecutionContext))
                 .expects(*,*)
                 .returning(Future.successful(Some(cachedResponse)))
                 .once
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue
  }
  
  it should "return value store in cache" in new UnderlyingProxyAndCacheStubs 
                                              with ProxyWithCache
                                              with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                        .when(*, *)
                        .returning(Future.successful(exampleResponse))
    
    (requestCache.getResponse (_:Req)(_: ExecutionContext))
              .when(*,*)
              .returning(Future.successful(Some(cachedResponse)))
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue shouldBe cachedResponse
  }
  
  it should "return underlying proxy if request isn't onto cache" in new UnderlyingProxyAndCacheStubs 
                                                                  with ProxyWithCache
                                                                  with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                        .when(*, *)
                        .returning(Future.successful(exampleResponse))
    
    (requestCache.getResponse (_:Req)(_: ExecutionContext))
              .when(*,*)
              .returning(Future.successful(None))
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue shouldBe exampleResponse
  }
  
  trait ProxyWithCache { this: UnderlyingProxyAndCache =>
    val proxy = new AemetWebServicesProxyWithCache {
      override val underlying = underlyingProxy
      override val cache = requestCache
    }
  }
  
  trait UnderlyingProxyAndCacheStubs extends UnderlyingProxyAndCache {
    override val requestCache = stub[HttpRequestCache]
    override val underlyingProxy = stub[AemetWebServicesProxy]
  }
  
  trait UnderlyingProxyAndCacheMocks extends UnderlyingProxyAndCache {
    override val requestCache = mock[HttpRequestCache]
    override val underlyingProxy = mock[AemetWebServicesProxy]
  }
  
  trait UnderlyingProxyAndCache {
    val requestCache: HttpRequestCache
    val underlyingProxy: AemetWebServicesProxy
  }
  
  trait ExampleValues {
    val exampleResponse = "example"
    val cachedResponse = "cached-example"
    val exampleUrl = "http://localhost"
  }
}