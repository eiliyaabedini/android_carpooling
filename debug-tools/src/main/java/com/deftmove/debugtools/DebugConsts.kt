package com.deftmove.debugtools

import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location

object DebugConsts {

    val Home_Address = Location(
          name = "Puškina iela",
          address = "Puškina iela, Riga, Latvia",
          coordinate = Coordinate(latitude = 56.9423835, longitude = 24.1223745)
    )

    val Work_Address = Location(
          name = "Gustava Zemgala Gatve 52",
          address = "Gustava Zemgala Gatve 52, Vidzemes priekšpilsēta, Riga, Latvia",
          coordinate = Coordinate(latitude = 56.9710326, longitude = 24.167821300000004)
    )

    val accounts = mapOf(
          "" to "",
          "omegasoft7@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgxODQ5MjE1LCJpYXQiOjE1Nzk0Mz" +
                "AwMTUsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiI4NDQ5ZjFiNi05Zjk3LTRiMWYtYWMwNS0yMmE5NDQ2NTY1MWIiLCJuYmYiOjE1Nzk0MzAwMTQsInN1YiI6I" +
                "jQ3ZWEyZmVhLTFmMDctNGQ5Yy1iYmZhLTFhOGIwYjc0MTcxOSIsInR5cCI6ImFjY2VzcyJ9.HJzxN9LuRsOIIt7whir3JlTUn1_kEriyrShSQ7Sss8IXBId" +
                "-ONtK6z3ZW0k4Zd5ZQ_G7oaMoFE2UGQ4uxmfSzg",
          "omegasoft7+1@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg3OTU2LCJpYXQiOjE1Nzk4" +
                "Njg3NTYsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiJkNWYyOTcxZC02NmFkLTQ4NjEtYTI4NS1jYmEyM2Q2NDA0YjYiLCJuYmYiOjE1Nzk4Njg3NTUsInN1YiI" +
                "6IjI1OGE2MzNmLTIwMWQtNGZhZC04MGZkLWFmMjI5OGNhOTJhZSIsInR5cCI6ImFjY2VzcyJ9.8f3GV1uPodEfIvyvD11d3gv7FFlQ8xkj89LEa1zcWz4WN" +
                "suPk-KoGKeWYqEgfISnFscFJjdrAfrhKYLAat7rJA",
          "omegasoft7+2@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4MDk2LCJpYXQiOjE1Nzk4" +
                "Njg4OTYsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiI3M2JmMzVjYy04MjMzLTQwMzItOGFkOS02YzQ1YTU4MzY2OGUiLCJuYmYiOjE1Nzk4Njg4OTUsInN1YiI" +
                "6ImFmNmFiN2M1LTc0YjItNGI1NC1iNTkxLWE0MDA3NjEyOGY1MSIsInR5cCI6ImFjY2VzcyJ9.NxMkLObKR2RFHPBTWgmcuR6nAEgNVVqVKtGlYzsfwwOHY" +
                "FmQ4yfdFui8X_KSO1VINTFZuiPyR23rrrxUQRuBRg",
          "omegasoft7+3@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4MjE3LCJpYXQiOjE1Nzk4" +
                "NjkwMTcsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiJlZDdjNDc2OC1jZWIwLTRlMzYtOTMwNS0zMWEwNDY5ZDQyOWQiLCJuYmYiOjE1Nzk4NjkwMTYsInN1YiI" +
                "6IjA5MzkxODc0LTZlNmQtNDFjMy05YmUyLTAxNTdjYzdmZjhjNSIsInR5cCI6ImFjY2VzcyJ9.iir2vZf1R_dR2xVAZKNHg-XU8DZFhk_NRv6XN7GRLK3gV" +
                "CWPzUbMu5-9A6YWEBsdr3b8x5mU611RtRc6xkG_aw",
          "omegasoft7+4@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4NDAzLCJpYXQiOjE1Nzk4" +
                "NjkyMDMsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiIxNzlmOTI0Yi1lNTc1LTRlYzYtODNkZC0xN2ZkYTZiMjJkOGMiLCJuYmYiOjE1Nzk4NjkyMDIsInN1YiI" +
                "6IjI1OTI2OTJmLTBhMzUtNDgwNS1hOTEwLWMzYWY0YmE0MmQ1YiIsInR5cCI6ImFjY2VzcyJ9.BsQMSYMTaFyE-EcgqYrjE1UJ9BPP0zmmzrilZVlhtQy-G" +
                "_lVhuMhARK4XgJrnLOXhmTk4tbz42OL1qeAG3Hqtw",
          "omegasoft7+5@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4NDU3LCJpYXQiOjE1Nzk4" +
                "NjkyNTcsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiIwZTJjNGMzYy1jNDdkLTQ0ZmUtOTE4NC04ZDVmZjQyMTBlMGIiLCJuYmYiOjE1Nzk4NjkyNTYsInN1YiI" +
                "6IjNlY2FlMzdiLTNiNTAtNDEwZS04NWNiLTBjN2ViMWQ4N2EzZCIsInR5cCI6ImFjY2VzcyJ9.pcU9h8v5Ux23p3MIcAqTQVLF0N6Ipdx3XR_WCQzYUqaer" +
                "kfME2shD7bIhNhCajNhc8OA7ShFwluWPBI6BJxA-g",
          "omegasoft7+6@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4NTEyLCJpYXQiOjE1Nzk4" +
                "NjkzMTIsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiJlOGU0YTQ4NS01MTBjLTRjMzMtOGY2ZC05MzI4MmE0YjdhNjYiLCJuYmYiOjE1Nzk4NjkzMTEsInN1YiI" +
                "6IjRiYmU4ZjdkLTk0MWQtNGU0Mi1iNzM5LWRhZTNhNDFkZTc0MyIsInR5cCI6ImFjY2VzcyJ9.oAtrfeff-foyE6OJPwmrv9AHCzrPEClAxXfBPxsCYzSTU" +
                "ZBz6OM4um0rF_YjPFzpcAO6_9s3SftQunxjbn3dsA",
          "omegasoft7+7@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTgyMjg4NTY1LCJpYXQiOjE1Nzk4" +
                "NjkzNjUsImlzcyI6ImNhcnBvb2wiLCJqdGkiOiI5ZmYwMDg4OC1lNDYwLTQ5NTEtOGQzMC1hNTJhMTIzZjlhNmUiLCJuYmYiOjE1Nzk4NjkzNjQsInN1YiI" +
                "6Ijk0ZGRlNGZkLTg4MzktNDQyNC1hYjM1LTVlZTkyN2E1OTFjNCIsInR5cCI6ImFjY2VzcyJ9.ihC4eDna32PizRUT_x82rW8nT5RtRi3k670w29AdEWEon" +
                "PCMQ66cyfOdMEh61s5QK04gXlEmdCSHq2Muu7sBqQ",
          "omegasoft7+8@gmail.com" to "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJjYXJwb29sIiwiZXhwIjoxNTg1NzYwMTA5LCJpYXQiOjE1ODMz" +
                "NDA5MDksImlzcyI6ImNhcnBvb2wiLCJqdGkiOiI4NjE1MWQ1ZS1kMGRkLTRkNGUtOWRlZC03MjcwNGU3NGRhYzYiLCJuYmYiOjE1ODMzNDA5MDgsInN1YiI" +
                "6ImJhYjVlMzcwLWFjNmEtNGJiMC05ZTYyLWU3ZWI3N2NhNDhlZSIsInR5cCI6ImFjY2VzcyJ9.v1fNOfShbWNBbq0yBn6Vumn1FUAHx-VP2Xgk1-vnPmh_y" +
                "znipTnEflkClckNIMibfpgDOB4UgyTqQKiJ5-gzRA"
    ).toSortedMap()
}
