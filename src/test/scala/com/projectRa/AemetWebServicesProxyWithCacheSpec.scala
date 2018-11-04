package com.projectRa

import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import utils.HttpRequestStore
import dispatch._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.Matchers

class AemetWebServicesProxyWithCacheSpec extends FlatSpec with Matchers with MockFactory with ScalaFutures {
  it should "get response from request store" in new UnderlyingProxyAndStoreMocks 
                                                      with ProxyWithCache
                                                      with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                    .expects(*, *)
                    .never
    
    (store.getResponse (_:Req)(_: ExecutionContext))
          .expects(*,*)
          .returning(Future.successful(Some(cachedResponse)))
          .once
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue
  }
  
  it should "return value stored for request" in new UnderlyingProxyAndStoreStubs 
                                              with ProxyWithCache
                                              with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                    .when(*, *)
                    .returning(Future.successful(exampleResponse))
    
    (store.getResponse (_:Req)(_: ExecutionContext))
          .when(*,*)
          .returning(Future.successful(Some(cachedResponse)))
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue shouldBe cachedResponse
  }
  
  it should "return underlying proxy if request isn't onto storage" in new UnderlyingProxyAndStoreStubs 
                                                                       with ProxyWithCache
                                                                       with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                    .when(*, *)
                    .returning(Future.successful(exampleResponse))
    
    (store.getResponse (_:Req)(_: ExecutionContext))
          .when(*,*)
          .returning(Future.successful(None))
          
    (store.putResponse (_:Req, _:String)(_: ExecutionContext))
          .when(*,*,*)
          .returning(Future.successful(false))
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue shouldBe exampleResponse
  }
  
  it should "put request on store if it isn't onto storage" in new UnderlyingProxyAndStoreMocks 
                                                               with ProxyWithCache
                                                               with ExampleValues {
    
    (underlyingProxy.getResponseUsingApiKey(_:Req)(_: ExecutionContext))
                    .expects(*, *)
                    .returning(Future.successful(exampleResponse))
    
    (store.getResponse (_:Req)(_: ExecutionContext))
          .expects(*,*)
          .returning(Future.successful(None))
          
    (store.putResponse (_:Req, _:String)(_: ExecutionContext))
          .expects(*,*,*)
          .returning(Future.successful(false))
    
    proxy.getResponseUsingApiKey(url(exampleUrl)).futureValue
  }  
  
  trait ProxyWithCache { this: UnderlyingProxyAndStore =>
    val proxy = new AemetWebServicesProxyWithCache {
      override val underlying = underlyingProxy
      override val requestStore = store
    }
  }
  
  trait UnderlyingProxyAndStoreStubs extends UnderlyingProxyAndStore {
    override val store = stub[HttpRequestStore]
    override val underlyingProxy = stub[AemetWebServicesProxy]
  }
  
  trait UnderlyingProxyAndStoreMocks extends UnderlyingProxyAndStore {
    override val store = mock[HttpRequestStore]
    override val underlyingProxy = mock[AemetWebServicesProxy]
  }
  
  trait UnderlyingProxyAndStore {
    val store: HttpRequestStore
    val underlyingProxy: AemetWebServicesProxy
  }
  
  trait ExampleValues {
    val exampleResponse = "example"
    val cachedResponse = "cached-example"
    val exampleUrl = "http://localhost"
  }
}