package rehanced.com.simpleetherwallet.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rehanced.com.simpleetherwallet.APIKey;
import rehanced.com.simpleetherwallet.interfaces.LastIconLoaded;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.utils.Key;
import rehanced.com.simpleetherwallet.utils.RequestCache;
import rehanced.com.simpleetherwallet.utils.TokenIconCache;

public class EtherscanAPI {

    private String token;

    private static EtherscanAPI instance;

    // EIP155 대응
    public static int eip155_block = 845000;
    public static int chainId = 31102;
    public static int eip155Testnet_block = 204000;
    public static int chainIdTestnet = 131102;
    public static boolean useEip155 = false;

    // Test Net
    public static boolean useTestNet = false;
    public static String pathTestNet = "";

    public static EtherscanAPI getInstance() {
        if (instance == null)
            instance = new EtherscanAPI();
        return instance;
    }

    public void getPriceChart(long starttime, int period, boolean usd, Callback b) throws IOException {
        // get("http://poloniex.com/public?command=returnChartData&currencyPair=" + (usd ? "USDT_ETH" : "BTC_ETH") + "&start=" + starttime + "&end=9999999999&period=" + period, b);
    }


    /**
     * Retrieve all internal transactions from address like contract calls, for normal transactions @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getNormalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getInternalTransactions(String address, Callback b, boolean force) throws IOException {
        /*
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_INTERNAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS_INTERNAL, address))).build());
            return;
        }
        get("http://api.etherscan.io/api?module=account&action=txlistinternal&address=" + address + "&startblock=0&endblock=99999999&sort=asc&apikey=" + token, b);
        */
    }


    /**
     * Retrieve all normal ether transactions from address (excluding contract calls etc, @see rehanced.com.simpleetherwallet.network.EtherscanAPI#getInternalTransactions() )
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentTransactions#update() or @see rehanced.com.simpleetherwallet.fragments.FragmentTransactionsAll#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getNormalTransactions(String address, Callback b, boolean force) throws IOException {
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TXS_NORMAL, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://esn-api.topmining.co.kr/" + pathTestNet + "get_transaction?account=" + address)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TXS_NORMAL, address))).build());
            return;
        }
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_transaction?account=" + address, b);
    }


    public void getEtherPrice(Callback b) throws IOException {
        get("http://esn-api.topmining.co.kr/" + pathTestNet + "get_price", b);
    }


    public void getGasPrice(Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_gasprice", b);
    }


    /**
     * Get token balances via ethplorer.io
     *
     * @param address Ether address
     * @param b       Network callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#update()
     * @param force   Whether to force (true) a network call or use cache (false). Only true if user uses swiperefreshlayout
     * @throws IOException Network exceptions
     */
    public void getTokenBalances(String address, Callback b, boolean force) throws IOException {
        if (!force && RequestCache.getInstance().contains(RequestCache.TYPE_TOKEN, address)) {
            b.onResponse(null, new Response.Builder().code(200).message("").request(new Request.Builder()
                    .url("https://esn-api.topmining.co.kr/" + pathTestNet + "" + pathTestNet + "get_token?address=" + address)
                    .build()).protocol(Protocol.HTTP_1_0).body(ResponseBody.create(MediaType.parse("JSON"), RequestCache.getInstance().get(RequestCache.TYPE_TOKEN, address))).build());
            return;
        }
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_token?address=" + address, b);
    }


    /**
     * Download and save token icon in permanent image cache (TokenIconCache)
     *
     * @param c         Application context, used to load TokenIconCache if reinstanced
     * @param tokenName Name of token
     * @param lastToken Boolean defining whether this is the last icon to download or not. If so callback is called to refresh recyclerview (notifyDataSetChanged)
     * @param callback  Callback to @see rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview#onLastIconDownloaded()
     * @throws IOException Network exceptions
     */
    public void loadTokenIcon(final Context c, String tokenName, final boolean lastToken, final LastIconLoaded callback) throws IOException {
        if (tokenName.indexOf(" ") > 0)
            tokenName = tokenName.substring(0, tokenName.indexOf(" "));
        if (TokenIconCache.getInstance(c).contains(tokenName)) return;

        final String tokenNamef = tokenName;
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "token/images/" + tokenNamef + ".png", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (c == null) return;
                ResponseBody in = response.body();
                InputStream inputStream = in.byteStream();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                final Bitmap bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                TokenIconCache.getInstance(c).put(c, tokenNamef, new BitmapDrawable(c.getResources(), bitmap).getBitmap());
                // if(lastToken) // TODO: resolve race condition
                callback.onLastIconDownloaded();
            }
        });
    }


    public void getGasLimitEstimate(String to, Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_estimategas?to=" + to + "&value=0xff22", b);
    }

    // 현재 블록이 EIP155블록에 도달했는지 확인하여 useEip155를 활성화 시킨다.
    public void checkBlockNumber() throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_blocknumber", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 블록번호를 가져오지 못한 경우
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    int blockNumber = new JSONObject(response.body().toString()).getJSONObject("result").getInt("blocknumber");
                    if ((useTestNet && blockNumber >= eip155Testnet_block) || (!useTestNet && blockNumber >= eip155_block)) {
                        useEip155 = true;
                    }
                }
                catch (JSONException e) { }
            }
        });
    }

    public void getBalance(String address, Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_balance?account=" + address, b);
    }


    public void getNonceForAddress(String address, Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_transactioncount?address=" + address, b);
    }


    public void getPriceConversionRates(String currencyConversion, Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "get_exchange?currency=" + currencyConversion, b);
    }


    public void getBalances(ArrayList<StorableWallet> addresses, Callback b) throws IOException {
        String url = "https://esn-api.topmining.co.kr/" + pathTestNet + "get_balances?accounts=";
        for (StorableWallet address : addresses)
            url += address.getPubKey() + ",";
        url = url.substring(0, url.length() - 1); // remove last
        get(url, b);
    }


    public void forwardTransaction(String raw, Callback b) throws IOException {
        get("https://esn-api.topmining.co.kr/" + pathTestNet + "send_rawtransaction?data=" + raw , b);
    }


    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        client.newCall(request).enqueue(b);
    }


    private EtherscanAPI() {
        token = ""; // new Key(APIKey.API_KEY).toString();
    }

}
