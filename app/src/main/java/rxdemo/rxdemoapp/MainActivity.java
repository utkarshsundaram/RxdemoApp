package rxdemo.rxdemoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG =MainActivity.class.getName() ;
    private Observer<String> animalsObserver;
    private Observable<String> animalsObservable;
    private Observer<String>animalsFilter;
    private Observer<Notes>notesObserver;
    private Observable<Notes>notesObservable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Observer is the counter part of Observable. It receives the data emitted by Observable
        animalsObserver = getAnimalsObservable();
        animalsFilter= getAnimalsObservableWithFilter();
        //Observable is a data stream that do some work and emits data
        animalsObservable = Observable.just("Ant", "bee", "Cat", "Dog", "Fox");
        notesObservable=getNotesObservable();
        notesObserver=getNotesObserver();
        simpleObserverandObservable();
        observableWithFilter();
        customNotesObservable();
    }

    private Observer<Notes> getNotesObserver()
    {
        return new Observer<Notes>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(Notes notes) {
                Log.d(TAG, "Notes: " + notes.note + notes.id);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All notes items are emitted!");
            }
        };
    }

    private void customNotesObservable()
    {
      notesObservable
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(notesObserver);

    }
    private Observable<Notes> getNotesObservable()
    {
        final List<Notes> notes = prepareNotes();
       return Observable.create(new ObservableOnSubscribe<Notes>() {
           @Override
           public void subscribe(ObservableEmitter<Notes> emitter) throws Exception {
             for(int i=0;i<notes.size();i++){
                 if(!emitter.isDisposed()){
                     emitter.onNext(notes.get(i));
                 }
             }
             if (!emitter.isDisposed()){
                 emitter.onComplete();
             }
           }
       });
    }

    private Observer<String> getAnimalsObservableWithFilter()
    {
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "FilteredName: " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All filtered items are emitted!");
            }
        };
    }

    private void observableWithFilter()
    {
        //The bonding between Observable and Observer is called as Subscription
        //Schedulers decides the thread on which Observable should emit the data and on which Observer should receives the data i.e background thread, main thread etc.,
        animalsObservable
                .subscribeOn(Schedulers.io())//This tell the Observable to run the task on a background thread
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return s.toLowerCase().startsWith("b");
                    }
                })//This tells the Observer to receive the data on android UI thread
                .subscribe(animalsFilter);//Subscribe observable into observer
    }

    private Observer<String> getAnimalsObservable()
    {
        return new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
                compositeDisposable.add(d);
            }

            @Override
            public void onNext(String s) {
                Log.d(TAG, "Name: " + s);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "All items are emitted!");
            }

        };
    }

    private void simpleObserverandObservable()
    {
        //The bonding between Observable and Observer is called as Subscription
        //Schedulers decides the thread on which Observable should emit the data and on which Observer should receives the data i.e background thread, main thread etc.,
        animalsObservable
                .subscribeOn(Schedulers.io())//This tell the Observable to run the task on a background thread
                .observeOn(AndroidSchedulers.mainThread())//This tells the Observer to receive the data on android UI thread
                .subscribe(animalsObserver);//Subscribe observable into observer
    }


    private List<Notes> prepareNotes() {
        List<Notes> notes = new ArrayList<>();
        notes.add(new Notes(1, "buy tooth paste!"));
        notes.add(new Notes(2, "call brother!"));
        notes.add(new Notes(3, "watch narcos tonight!"));
        notes.add(new Notes(4, "pay power bill!"));

        return notes;
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(!compositeDisposable.isDisposed()){
            compositeDisposable.dispose();
        }
    }
}
