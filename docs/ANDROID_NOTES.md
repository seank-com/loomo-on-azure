
## Run code on a seperate thread

```java
new Thread()  {
  @Override
  public void run() {
    // Put code here
  }
}.start();
```

## Run code in UiThread

```java
runOnUiThread(new Runnable() {
  @Override
  public void run() {
    // Put code here
  }
});
```


 