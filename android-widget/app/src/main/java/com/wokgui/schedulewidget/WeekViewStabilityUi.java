package com.wokgui.schedulewidget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

final class WeekViewStabilityUi {
    private WeekViewStabilityUi() {}

    static String script() {
        final String data = "H4sIAN8ToGoC/9U923bbSHLv/gp6fJYAxiBN6i5yIEWy7HhybI+P5ZnNiVbH2ySaItYgwAAgKQ3J78n+QV7ylPmxVHU3QNy6AVLyJtE5kohG9a26um5d1XzW"
            + "/SP3oo5k3jBzf043ls0bmJwoe8kWNhjPSF45n+4v2168LSr/95tDFdUQGjutED78d7RvFKulKAR0FNBz/OV/TqHivG/2SdgMazQKv+GZdKFGO2YqCGe0/K1Qa"
            + "+l4YNS4+ffr625vP1z//8tHSjtr7WrE/l0aNkXPveHevH4YutUbEDakMLAijj45HP1PPpoEKNKRRBE2GvwxCGswB1pu5bhE0XsDG0PU9qvvGkqOl8S/Xv3xs"
            + "T0kQUp19DKMAmnNGDwBjrOXtuD6xL+y5XrqUSBTF9i88O/Ad+3o4pvbMpW3RBPGG1L4W09CN1UpbrrWSrgHVJBqOdZqMfble11jVZMghmVPo75fB33D+kmHn"
            + "BykqZQdZgqlkbFsNCdYCqVhPppSgtR1C4y57a1mM9hTNzJz3xLuTrwWn0YAsrLI1+NXZYL/PQX0rtW5QTyxKX4zSb7vQ34zcURibZlNttcoWUU8735T0tFGg"
            + "rfOrxwq3QdYsNc7HUV16xjG9PZK4okAfBSb1TBua4Eh0rXhdYry5MW6o19Ndgbtzm/ZGgWqvTRxvFtFfRnpUOm/e29S6ZgSpRzCjTqfX6WhARFPgXrrWg48T"
            + "MtU/ziYDGii4ZEOf3nRuV6uO8eNR5yU8dNlDfxtMAIcZftMnCSLFsD6QaNweub4f6JNXRx3DgPWxgcEGkb5najDYlzDMlwJ48ieAyAOsZcx36rtOOLZsfzib"
            + "UC9qDwNKIvrGpfika2H04FKtZBK8XtuxLQ05fprbX7M60ioRvY9e+14E7Vt/LcHmOJq4jYFvPzTaY0qAhZetG4BR524c9U4OpvfPncnUDyLiRf1SUMCFDZhp"
            + "Rf60t7dXA94PHVyOXkBdEjlzWlXBdoBYyENv5NLKxonr3HktJ6KTsDcEHNCgqsbfZmEErLI15EirVWtdB7GNcbcctxMS3Dler1MTsdWALsjjllixbhVwFBAv"
            + "HPnBpOcBj3/MPF/EEv4y8spnGvAhdaqJAomHzCK/Cm7gR5E/6XX3q5usi+btEKLGyBxUsze2EzVe4Lb94Nv0kgRqKmh0Gse1N1nvaHrfOKkCX+8wyEY7fng9"
            + "9p0hDZd3ZNrLbP+naFaCC8drLRw7GtffFIeAiMNqvI1gV7dC53faax8dBHSy1VZqdw4fhWhe99PYj3z5vEVnBzU2yf89Rgi7B+RNi3VWswKn+5bYyMePpGVe"
            + "F+RjNAt7dDKNHpYxlrK7uWrnAlm99meglpmVcJcz9xuHDZf/b9aJb69up/OnygXaEOX+SV02y6T/FnzstBp04AcgRWEX4lYPQbuxG3MS6K3WwJ1R80Xn5Ph4"
            + "dGrUa6UVENsBCqkxwgEZfrsL/Jln915Qe3Q0GlXVGPquH/R2GFuKOZ3UYU4MfiE0s852akG7+zjJFvk2eXjvhNFSob+ta7bRaLOPfBOV76G7wLFbsCtgI0UU"
            + "aN2dTbywdwhcEgl0Qu71jtkF46RRR2soiDK1JuvSUdQ7fCRnKp8sPDgTulRwzdrN8o8f/cVn4rhLtapNBrCBwFKrJBk28xqo4uxkb6vtlN4h3aOTk7cHlTvk"
            + "95bj2fS+d1qtF/gO4rFF54DPsIYql2cN9fREjs5Ha84lq3jlR/87i9jNrmLMMPbqcugYgYfVomWjarNPuLX1FtQz8Y/xfUlJiJP9RJi8uDp98/rt2+p698ij"
            + "UWiJ+UJJbbrtfge63ZIQ1ZSI6vk/A68FRX3MeBOw2qVytRl57G3QODy0O/bwUdshMwjYDsoBCM3x+4zArDnQH5ePXxpQ/jxnQp5mIduh60ef/cWyXHCeHBUE"
            + "Z+aprvxh3XycTdQSrJ7b4HpMKUjH+PEdJfbyaTxGu6nIacV375HSXzHHxnhv+UgVfWujK3ElNfZPvuPMkqd/fSJRxplNp473qPMdZnXp3y/r2iYvhif2vj2q"
            + "JU7GxPYXsBhdqI0C6UVn1D3eI92D7zCHL07kSnTsLcloq+5fjMD6uJ7SoUPc12gghXVGlHUO7D8pqZaNaMiU8k/EBUj6DiTxssrG7Wzn33ji3caX6cKeX86g"
            + "eU/if/iHeHK2wz2ZTn+jQQh7/2dv5C+f0pXEXKg1tF1upr84JicHpwfb+A1Pnto031JATgOKvtWL6fSLPzV3qXoJ73aq+GfHvqPRI6qyvf6I+h9oRJbfSQv+"
            + "pwm1HaKDEiScz/unnem9ITkQq6tpHZ88RtPawaOeItXD0yypZhwaWW4av+ISdr/Kz18s+2sRz8kxJx6AtWHLU89+PXZcW+eHk0ZJkEpyOEu9cBZQdrqpy6Jw"
            + "xBnnlATQy0dAwXPLynRq1BhCDdrYDOoeyOwa1vuKjsjMjSTn++I8+Wbp9U7MEA+Ge1r3GA+6TegfPp/g57UJ70+T9yep96fs/e1Wp9nJ2CSD4ifQxEoCN8ww"
            + "GFoXQUAe2k7I/uukvWnFOE8/9Uqm3pf2YtORVbNCjCvYDbfs6N+xznRAjPMyRp04addhuDfObbPJ/7fZS2O1gr42jwx/kgrwagOOD2tju3iBKUqsMHqzQTSQ"
            + "UCSN8JBIHcTP0KXEs7A2cA/YtMAL9njggx6ajoEYyMw95NNbrUqQWj75EGcohZfOvpxQfNeuTyg3t6pmQzKhFrTXdql3F40ty9prNvGZzmnwEE8/P21NMwCS"
            + "YS2ZbrOZnWoOBqdYPhBgHM9xGMYyPW5es58KvSKloV07xk9xdoaRcootKqUaGHL0MKX+qBGysVqWNvOAkqE5W1utnmcXh8EAqbP/AtE/HRtip0lC9FLEOSbe"
            + "HbWtdHVgrKeqOgMSUlEhJujjHEHjlu6KLS0WlhEsW74KYqT3Vpq9lYNy1OBIQI/3YJl0ev8kYxCIE3jpK4kijeStCGQRkOlHP5iAwvs7oxOp1BOUIGJAvbgO"
            + "LJEWtwY0kX/d/vrVi6kPA1tlwaYxynHP59uQQ+Pop0Az8vDbCgov0PjzDI03m2UkDqw9RaNnx4as7UYJ705T67F5Klv8DWXlyVsGX67HqfhGCuuziHFHwKb7"
            + "oEdjJzTBqGEqTCjpsMBZlEQMHZQScFmhWNQc5Yjw4jJ/VZZaLFF/KxnrB6CGElf3FDpMKlyxfIfEAYsxy/Neau3GdYTkpKmqUC+pon2igePbDe2lp9BaPKjV"
            + "Pde6f/w9oI0xhXXQetDZH3+fxI/bKRhMJ91FQNQkAo6/CI2wjaL87zOQvNfUpcPID3RtY2a0Q8rGxb2U+z2XhFHLH7Vwm2rKHlDbRhNOaA2jwJ/o5f1duK6i"
            + "S00tFnBHvuNdWXGXbWAYtj62zl698wPiBDRcTdlSrjgBeK+cdkTDSM/ERjL1AUQmQ41UbUj1Z6Q+Z4IsI0Bh3DPowWHjlFNCqJnaaYMPpYEHK1jwb9SBSg2M"
            + "DjwVBOrJMKvCHw4mDhRODFLNaI/84A0BrqMH/oKJQBmD5Pj0LIDL00Ps4odxAQo8w8tMN96vIFelDJE37njTGXDRQg9sBuylJm2B102mwx7lk+EqHgMC3sUU"
            + "h0+c/csyOjL9FOtIWV62HpjQb/DYDg9yqUcBd1xl0EzdUI1WjNg5s46bTZUMZIWg3BrqtoTASjRli6PvpnN7nnxqz4k7o70sYL9us6AtxY12k0a7hUYBrKrJ"
            + "R8llubQV7xR1ZfVkdcrL1dZAlTnAvGHXU+KpLPaxlFMzz88Xf9oY72kyUfh8nAjB0nQbkZsDY9h0c0cjEZJ++fCzrbOQczbU9+gWD+R9YTMyf1nb8WBLvPvy"
            + "4T1jkRew8H/8x6wR0gkB6m4AM8QMkoYPZiBKA1bgD8fU++O/Bkgjw3HUwND7n7CThmNbP+SG9QNY1SQMebkoOrv46RXCn2kSk+Hx817LtQNsvb+9kXjN8mkw"
            + "hlFuAzxP8nEUijxLqJkF6Bxj2TnahdYnQzwwTR5T5gQUFRgOK2xfGCEOxhJPO1rAU1jn6JLYd1RF7cOFfD1Sc7mMPC6Png8XCgwAwAZRKoeMSwbUZYR5LQhy"
            + "5jmw3YAK+XIw/MDTG8f7nboLpExN7l8YLtJCErDK2jeyxbxQot8s8HTsIooCZzADOtBI4JAWq6CZvLF6xKhYEGZj8Nn96qjWBFBoJWjsJ8sTfxBr1GbbD2Vf"
            + "O/LvoFFd22SGoX9aM6ERKe/Ad/KFLNkZIgGMbeECO0XSYNwI/2SQjlSfJkW5TUtBIZUTI769Ig+sT06KWGLgn0x/er6EqZrtgE5dAhLuVeO//zNhgjcXravb"
            + "V46ZBWiVvZcPmwWQycfNXqdGzZ4N9jc77kLR9xq4KhHywRuKRRdZcfou7tbA9yOrItNu00GSQgi12izDFrXW5PNqtVzLHYspwPZFos0pmWumisXTXuN30o4y"
            + "lYxl+vFStJEFSU+n/boa5KoUpFxr4jB5USNKMYX4PXf27vXLklYTzOeSVbG+sbXWVcuLqlyPZRaNCXayqMsVX+WKJU6ep5fGRZTS6PWmG9C0tB00V7XQYGnh"
            + "/LhR4qjxLJZFicecXZN/dDz9wOR5nVBptdozFEwsdfogERjM9YIu/FT6MTPTSDtLchmPfr+Mo5StFXVlkeiZHrkzPdtl7hChv60j9PE8Kzuap9lz1R7MyikR"
            + "1/UX1LZugCZN7RJ+X8PvlXabOFU9uQcAeKuoD5bE0J3ZNNRT28kw8nurtMZmwxlGavOl6u4y97KyR6hJnLRlGv2GSQT8zoW039/gZbqhHjBotjP6wRkGfkTC"
            + "b7rcPeHAugESwchke/6SBKhb8cI0FzD6OW2yn/FlSsVY9p6M+PqCt5RE6DIz1K9rt3vlzwawIQBJ418dQ/GuvMX1lmnlbPehjq48id/ai73s9rRugxnFmrnX"
            + "0y4arxqwifbFJ/iF7XSQeoLfK21d0+HNW+dWzuMazzQYUuAyj2tRgeoy8pRjHP0KcsU49kFrhjkggdorIGJvAHSK2Zxy4FTKp9xtgj2vVs+hV/jLGlSbtADY"
            + "9kAvFz1dOwMX9h1oC7wuNgccL6RBdElHfkCxAh+oIXMADXmSr4VNl3mZUonA8nmIRoxl3JrsngPbmUMrAoozxI8YF6Dlu+rjeNJRO3EX62dKSz60MpuwXy8c"
            + "hk0Z5BMpXmly3u0lCs1eiUKT0QAUyo2Y8sYXppV6pm665p65bx7cJi5vT8ahxfm7FNcDFhkK6B60UWxYcQE8SxGvvWTqVYyQc63BZaXW01hDNgGxQaM2t41E"
            + "GIbHusi7N8IbTxIPMmj7IJWd4TeLwtwoi/ODaiJQBRYNNJXIn34K/Cm5I/xQuV+ifq4TQkoTyqA2J8+6gFQbgJGTBquLQOzmI4Fl1qWMz2KlZvM5+58S/hiM"
            + "C7ZxqBeI3jA4bP5USXimwkZPOEvDXuIk7W1pWJeJcDnjBEKQszhiz1kzwh8HsKtV6vaomJWlL5SSHadUmPGlW1vEHxAvYpplF1CyB7/78Hug3YrXQr+zbtpt"
            + "sI/ctj9FLIQ84My3znx+cqFw6PEGNtEwB6sV77Md+hOq63N2vCbAbpxbAJkb0nMaHEOGB4i24t0+hx0R328k29h8DoB1MXhrDp9K9p/gUHPjto/dpjeJb0hP"
            + "O9D+aRTQFY+PoUzeV4zOW7xtKQVWN+CsH0cRhk4g4cew0L30fT1bMGbuHIRpsUFiyCjvx0jK4hJF/BiAfuV2FjtTzD5zAmfYlh4JcqaHHO7nCQs8jmgFq6Pt"
            + "iASw+wRy1yb2Uh6jNsIzWfdhWbjFbbtTqgFoym9JyOyiL2SgVGcH/n3FSQo0IHgEwAIT9e8BZSPRfCokqfBCyi8QsgTDKFQ4gpUSM8Hn0PVDGkbNZr4kPmIj"
            + "g3jgqKZBpwnzHgBJ1TmKSeKkrKz0ZK2C8pd2+xSi+1jpzeJW2X5NAaokuW3N+pQFveinvFIwVpkdj9grHv9v0JywmHvr7L7EVhaKiHkPWBoYFSEH254NLPqV"
            + "sWncvhacqGB3iyMSeStPapeWtsnvFeAJTYb8lV7h5LmXeDo409kuat7FSVxiojbGj+0c+ppqJo97fmfd5r06/okHReiKiMKb/VvjPLnMLi5iMcW9472OMlTV"
            + "s9VNHxSbPogjuHvHJx1l+N4yiVld14pDFWHkMGYW5wrNb3dZIPfiIFkzSnxj39FwpyMYTI5RywfMoxZsFoFrMFMeaWjp0NprfzIFjNo8X0TmezOwY2R5YBo/"
            + "/IZSVNdaLbwXBbV67QU9ooMR3kYIisWkioYC37t7dN+jqDVxbKfFE0j5KF4fXxwfvqkYBaKohJHeoHDhTVJYq9a8e2D95YfuX364TbFW0Oal0Uigs7BbCIFn"
            + "TPw5jQcMdmQq3b/FcudgsaqgedZjGpwDpg62iwPWtorA2fjLF6Elyz2Ametx7OOENoCZpAITJajshUOYTeMsfRMC2GaG2sc9Qv5j6QhfPDudsN356qbT2rs9"
            + "h7+ntz34e3jLPr66g9WHCcAGYI3Egft7BmobwDCoWtgNqesyDKA/x7PYCIpeov4CNH8wm5tNBh93cmigX9nbCNzztIE6hHlz8OksHKOl71leSeNrtJRSzQKb"
            + "PjRwYXi9JY5JRN8nHJDN9aZzy7NXcsVdKGYNSu0UpYaXFzwS1ooDjPcGPFhnGKPI55EEUOKjaZOHn/EeD0U0H7IvBlxq6rMBvYa3uNOlUPaDRybO8H0tYI/d"
            + "gJCCVauhSYYWebBumI/JPDSPbm/iqd2amJVkJaH4qNQVwhCxUPzDireF6PzkjcioDg3jvFgoTRVKVpC5L0WeFJrZQ+ssoZAhE8c/gUItsp/IKKKBFJgDnQE4"
            + "VjMxzswl0wrwnxh0s5nr9CzuUz18rqtafBbNJhtfs/lcdN3PLWms4TIauWbiBYhWM3krKgVQQMiDQllPnFGHGyGUcGkwCNBrklxdoL3k0s3UkhxQTRFJWdE6"
            + "lwG7d7CWH8XWnltHe+K5dFNz4WpIvbk8q+y2ltxVkh3agRZrkaVO+7MwJwIwcxbeGEv8u023DL6WBJfkwBhPGYqA6SXcSvrM7ZuaGVNlBlZ58lQZZPvr1/l2"
            + "6VNlrfSlaVN18oACCupKGF3E1wW9DQj6IEEshZhiP6FQXS9T3c0OrEAVTHe/s7k/fRat+5s8oHnsjFFMTZ72s4stgLDAM5mO9vVrijfGXyPA3G/K13zEHl00"
            + "Pswihq/4ZS2UdQ/wCnCfV2HjMZdDdJ8i1+5h22Y4G0QBpT3uYjO2c7LFl+B8wXi7DyT4RoOdLKzq003Wg2ZwCa8OEMS5CWNMnE6yVGeVZoFqZ0Act6JlcUEh"
            + "DMP2o2rYKz9SxNk+xw7B2M10m79UfUrQS44w7AL1zCj6TP6nfeKsxbW0Qxi0scyMvLw7AMn0hvModoatrauMmpSpwhoocZ+lbpTElKnYPF5YSPVXLFzWnFhQ"
            + "gHh+h3A6uzdflHxg6g16TXARuQ+UfT2HOQrI0OpUGVUwTLSpcLT1zKOyNCM0DjTjfEerSa4dyYwpYcdbpZaIVTREFF1MzqwQlMSfqGcsBfZghn2GPH3SCo1X"
            + "qWg46kFBfwBU8229hU2DxMfb5vQu5La4W9hCesuWaHgNiiYYuYrI2EkmDhjzgtiWZyXxZudAD5ZwjfujEXJMf/qy7NwlA/SO3X/T2jNLSn9E5EgTrmvNb4Bf"
            + "36D1U8Cg7Fk6G3xqmF3jpTa9z8DxW92s1AQeWrlqrbjapmNs/YEXPq0Sw787h3Hn2ipMUkOmuSQAIBcZe/hMif2QOmYp0VOSOlsletfRWEqFnETJyIw2p24k"
            + "I1QlF1cee6eunlIee+MFRHIRlbmnKD79xs8xhpGZzhUH55k7sBThQcZyXhFKM2eyJtfgep5NfBDvwGJJfQeUpFM2j2KMAzvV5u8yAUZzM1WBQW4XjhBQZm6o"
            + "vzXnaRZFnjcxhmYUCgm+FnkT56kDws3VgBgNxm+Ok7ciAMREP/uo3ZgjkLTyKoVr6qSUwobYbIo+mk32LIkU4zAGB+FreWH/jQw3DmsgUHRYwJbT4nnJOk56"
            + "xJkkAyjvGUGMGKKyYwat4EbsUjBlbErq3jHFwjFxHF6qzrAZyBUXPmL9pCvBx9VsJs02m7yoHCcJmCGgKvGyqVEaPnaDM7+w7Tf3Q8rDRG5NVvaRLsDGHzkY"
            + "k4nPIHrAdMwUXYFKFiVFot71mAT0kgy/zaZxxRDUtrjodhMa4tih+uCdqxsyNEN1poLxO70sBpxeNZLcdcg3N4cz+L+UN43YmDCYux5Re4rwXswg+MTvpLse"
            + "kh0zknwr/R1hJtnoIu2TjTbVbSdxLD4K1rfAylmfoPN2DcNc1KjGKSpXUxmHw+4VVLCwzfWDfFOZUOM9BqZZyisKpDcRJg/v2UGc1NjjAzP4vzInXXLznWbq"
            + "xz8SAzS2t849tfU9rsvVcNHFU0mHISzvyzrjOiT0dFTZU2l9flkl1N+vqq8+91pE8ps8Km9ihMVbTHarjzcxYvXBttVZ3LQ8cD8yFlH14i4qUI4tTYzFpKql"
            + "o1otDYzFQEkDB5XNPJ3FgEFZMf9xHdCaJOrSjSY4BoxkwwS0NKdOogwV8Z2OzZNbMYaVBbmJZWR+t1wBV9ZLw974BR/sIoxc8kmenxrGekuOLHxz31N7XK3y"
            + "X1+aVvLZjGxLerNa8atPZc5IPIXlrcXti7YZZsvSdjJ9l9k3/YJy3S+KsH4ZVa0lrCc/ncQ5ylAl9Y6yrwY1drkQabOwTzvBfoFyVMbjxlRnJtV9tJWxLuqo"
            + "zHUBAlsqjBPdBzy+VG2yi3qPv52txnUJ5an4mYR6hRZQEuc3sM4Giji/bLgkxn1doIdT5YheK+8/2+ZytbRXIrckJX4JsQw7XXuGdb6wqadzd+vRV5SvV05j"
            + "BbAt6KxQdytay5JVJf0Ij2XVgtVdnMLYd16gzyK3cotdr9ruOO6aO3x3dMsvzlA77PLpnJv5Tfg9y5mIECzDvRnxE6Zyb1+Z9MpmiZZmm5aHIK5l3sNQsUN3"
            + "Xnn8ir+dDpk33w1YccacBsweMcvOk9M1ntxfW7ZWitURC7je8tw4PYWd1ybOCt5pffIpxco1ygPXW6d8rX/kWmVTtFULuNGAtlzD/PR2c8wHqsXLXAXfL7ko"
            + "t5+9uLjWZZgl2lxfIoX7hUOafloc9Ascol9Kl/2SgJV+McVGfv1ROoehwJ93XnCJC1MgE6w+5oNDbPIJG8tcAaeK7NUEKiGBFyJpydU4nDNj4nVZQgWWZy4z"
            + "KFPuckLk8ZLq2RPnbtQJMam1X7LdsPQotGfir4O3xIv+M1keZ/IF3dU2cHLFWqoAqCH1JPjfslAU579tiqVZWqURODFxYrhSccETXlGc5k3HPOmYe4cd86jT"
            + "Mbt7nc7G3TEJMz2JVsxJWOYOZWlZHNnpA0mszCKOFO8s6ORn9DvPiZvY9I+kyLXZPex0ikSy2Ri4wD67FuZO1wqE8avTOGrva2Y+XXBt5LH4P1lwr6lOhwAA";
        try {
            byte[] zipped = Base64.getDecoder().decode(data);
            try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(zipped));
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
                return out.toString(StandardCharsets.UTF_8.name());
            }
        } catch (Exception e) {
            return "";
        }
    }
}
