package ch.epfl.sweng.erpa.services.GCP;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.util.Pair;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static retrofit2.Response.error;
import static retrofit2.Response.success;

public class GCPRemoteServicesProviderTest {

    private GCPRemoteServicesProvider grsp;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS) private Retrofit rf;
    Call<String> call;

    @Before
    public void setUp() {
        grsp = new GCPRemoteServicesProvider();
        call = (Call<String>) Mockito.mock(Call.class);
    }

    @Test(expected = ServerException.class)
    public void testErrorOnNon404Execute() throws IOException, ServerException {
        when(call.execute()).thenReturn(error(403, mock(ResponseBody.class)));
        GCPRemoteServicesProvider.executeAndThrowOnError(call);
    }

    @Test
    public void testEmptyOnErrorOptional() throws IOException, ServerException {
        when(call.execute()).thenReturn(error(403, mock(ResponseBody.class, Answers.RETURNS_MOCKS)));
        assertFalse(GCPRemoteServicesProvider.callAndReturnOptional(call).isPresent());
    }

    @Test
    public void testProviderName() {
        assertTrue(grsp.getFriendlyProviderName().contains("Google"));
        assertTrue(grsp.getFriendlyProviderDescription().contains("ERPA"));
        assertTrue(grsp.getFriendlyProviderDescription().contains("Google"));
    }

    @Test
    public void testCallAndReturn() throws IOException, ServerException {
        when(call.execute()).thenReturn(success("hi"));
        Optional<String> res = GCPRemoteServicesProvider.callAndReturnOptional(call);
        assertTrue(res.isPresent());
        String resString = res.get();
        assertFalse(resString.isEmpty());
    }

    @Test
    public void testEmptyOn404() throws IOException, ServerException {
        when(call.execute()).thenReturn(error(404, Mockito.mock(ResponseBody.class)));
        Optional<String> res = GCPRemoteServicesProvider.callAndReturnOptional(call);
        assertFalse(res.isPresent());
    }

    @Test(expected = NoSuchElementException.class)
    public void testExceptionOn404() throws IOException, ServerException {
        when(call.execute()).thenReturn(error(404, Mockito.mock(ResponseBody.class)));
        GCPRemoteServicesProvider.executeAndThrowOnError(call);
    }

    @Test
    public void testAsyncRequestSuccess() {
        Pair<Integer, Integer> p = testUpAsync();
        assertEquals(1, p.getFirst().intValue());
    }

    @Test
    public void testAsyncRequestFailure() {
        grsp = Mockito.mock(GCPRemoteServicesProvider.class);
        Pair<Integer, Integer> p = testUpAsync();
        assertEquals(1, p.getSecond().intValue());
    }

    private Pair<Integer, Integer> testUpAsync() {
        Call<Object> call = mock(Call.class);
        ArgumentCaptor<Callback<Object>> argCaptor = ArgumentCaptor.forClass(Callback.class);
        AtomicInteger exCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        grsp.executeRequest(call, resp -> {
            successCount.getAndIncrement();
        }, exception -> {
            exCount.getAndIncrement();
        });
        verify(call, times(1)).enqueue(argCaptor.capture());
        argCaptor.getValue().onFailure(call, new IOException());
        argCaptor.getValue().onResponse(call, success(new Object()));
        return new Pair<>(successCount.get(), exCount.get());
    }
}
